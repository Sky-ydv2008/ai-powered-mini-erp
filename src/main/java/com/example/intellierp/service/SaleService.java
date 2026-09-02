package com.example.intellierp.service;

import com.example.intellierp.dto.SaleCreateDto;
import com.example.intellierp.entity.*;
import com.example.intellierp.entity.enums.PaymentMethod;
import com.example.intellierp.entity.enums.SaleStatus;
import com.example.intellierp.entity.enums.StockTransactionType;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.InsufficientStockException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.CustomerRepository;
import com.example.intellierp.repository.ProductRepository;
import com.example.intellierp.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AuditLogService auditLogService;

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Page<Sale> getSalesPaged(Long customerId, SaleStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        LocalDateTime effectiveStart = start != null ? start : LocalDateTime.now().minusYears(1);
        LocalDateTime effectiveEnd = end != null ? end : LocalDateTime.now().plusDays(1);
        return saleRepository.filterSales(customerId, status, effectiveStart, effectiveEnd, pageable);
    }

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale record not found with id: " + id));
    }

    public Sale getSaleByInvoiceNumber(String invoiceNumber) {
        return saleRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with invoice: " + invoiceNumber));
    }

    @Transactional
    public Sale createSale(SaleCreateDto dto, User user) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("Sale must contain at least one item");
        }

        Customer customer = null;
        if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        }

        // Validate stock availability for all products before committing
        for (SaleCreateDto.SaleItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            int current = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
            if (current < itemDto.getQuantity()) {
                throw new InsufficientStockException("Cannot sell " + itemDto.getQuantity() + " units of '" + product.getName() + "'. Available stock: " + current);
            }
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber("INV-" + System.currentTimeMillis() % 10000000);
        sale.setCustomer(customer);
        sale.setUser(user);
        sale.setSaleDate(dto.getSaleDate() != null ? dto.getSaleDate() : LocalDateTime.now());
        sale.setTax(dto.getTax() != null ? dto.getTax() : BigDecimal.ZERO);
        sale.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO);
        sale.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : PaymentMethod.CASH);
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setNotes(dto.getNotes());

        for (SaleCreateDto.SaleItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId()).get();

            // Deduct stock
            int newStock = product.getCurrentStock() - itemDto.getQuantity();
            product.setCurrentStock(newStock);
            product.updateStatus();
            productRepository.save(product);

            SaleItem item = new SaleItem(
                    sale,
                    product,
                    itemDto.getQuantity(),
                    itemDto.getSellingPrice(),
                    product.getPurchasePrice(),
                    itemDto.getDiscount()
            );
            sale.addItem(item);

            // Record stock transaction in ledger
            inventoryService.recordStockMovement(
                    product,
                    -itemDto.getQuantity(),
                    StockTransactionType.SALE,
                    sale.getInvoiceNumber(),
                    "Sale Invoice " + sale.getInvoiceNumber(),
                    user
            );
        }

        Sale saved = saleRepository.save(sale);

        // Update customer spending metrics & CRM tier
        if (customer != null) {
            customer.setTotalOrders((customer.getTotalOrders() != null ? customer.getTotalOrders() : 0) + 1);
            customer.setTotalSpend((customer.getTotalSpend() != null ? customer.getTotalSpend() : BigDecimal.ZERO).add(saved.getTotalAmount()));
            customer.setLastPurchaseDate(saved.getSaleDate());
            customerService.evaluateTier(customer);
            customerRepository.save(customer);
        }

        auditLogService.logAction(user, "CREATE_SALE", "Sale", saved.getId(),
                "Completed sale " + saved.getInvoiceNumber() + " with total amount ₹" + saved.getTotalAmount(), null);

        return saved;
    }

    @Transactional
    public Sale cancelSale(Long id, String reason, User user) {
        Sale sale = getSaleById(id);

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BadRequestException("Sale is already cancelled");
        }

        sale.setStatus(SaleStatus.CANCELLED);

        // Restore stock
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            int newStock = (product.getCurrentStock() != null ? product.getCurrentStock() : 0) + item.getQuantity();
            product.setCurrentStock(newStock);
            product.updateStatus();
            productRepository.save(product);

            inventoryService.recordStockMovement(
                    product,
                    item.getQuantity(),
                    StockTransactionType.RETURN,
                    sale.getInvoiceNumber(),
                    "Cancelled sale: " + (reason != null ? reason : "Order cancellation"),
                    user
            );
        }

        // Revert customer total spend
        if (sale.getCustomer() != null) {
            Customer customer = sale.getCustomer();
            customer.setTotalOrders(Math.max(0, (customer.getTotalOrders() != null ? customer.getTotalOrders() : 1) - 1));
            customer.setTotalSpend((customer.getTotalSpend() != null ? customer.getTotalSpend() : BigDecimal.ZERO).subtract(sale.getTotalAmount()).max(BigDecimal.ZERO));
            customerService.evaluateTier(customer);
            customerRepository.save(customer);
        }

        Sale saved = saleRepository.save(sale);
        auditLogService.logAction(user, "CANCEL_SALE", "Sale", saved.getId(),
                "Cancelled sale " + saved.getInvoiceNumber() + ". Reason: " + reason, null);

        return saved;
    }

    @Transactional
    public Sale refundSale(Long id, String reason, User user) {
        Sale sale = getSaleById(id);

        if (sale.getStatus() == SaleStatus.REFUNDED) {
            throw new BadRequestException("Sale is already refunded");
        }

        sale.setStatus(SaleStatus.REFUNDED);

        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            int newStock = (product.getCurrentStock() != null ? product.getCurrentStock() : 0) + item.getQuantity();
            product.setCurrentStock(newStock);
            product.updateStatus();
            productRepository.save(product);

            inventoryService.recordStockMovement(
                    product,
                    item.getQuantity(),
                    StockTransactionType.RETURN,
                    sale.getInvoiceNumber(),
                    "Refund/Return for sale " + sale.getInvoiceNumber() + ": " + (reason != null ? reason : "Customer return"),
                    user
            );
        }

        Sale saved = saleRepository.save(sale);
        auditLogService.logAction(user, "REFUND_SALE", "Sale", saved.getId(),
                "Refunded sale " + saved.getInvoiceNumber(), null);

        return saved;
    }
}
