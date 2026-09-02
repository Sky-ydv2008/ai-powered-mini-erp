package com.example.intellierp.service;

import com.example.intellierp.dto.PurchaseCreateDto;
import com.example.intellierp.entity.*;
import com.example.intellierp.entity.enums.PurchaseStatus;
import com.example.intellierp.entity.enums.StockTransactionType;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.ProductRepository;
import com.example.intellierp.repository.PurchaseRepository;
import com.example.intellierp.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AuditLogService auditLogService;

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    public Page<Purchase> getPurchasesPaged(Long supplierId, PurchaseStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now().plusDays(1);
        return purchaseRepository.filterPurchases(supplierId, status, start, end, pageable);
    }

    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    @Transactional
    public Purchase createPurchase(PurchaseCreateDto dto, User user) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("Purchase order must contain at least one item");
        }

        Purchase purchase = new Purchase();
        purchase.setOrderNumber("PO-" + System.currentTimeMillis() % 10000000);
        purchase.setSupplier(supplier);
        purchase.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now());
        purchase.setExpectedDeliveryDate(dto.getExpectedDeliveryDate() != null ? dto.getExpectedDeliveryDate() : purchase.getOrderDate().plusDays(supplier.getLeadTimeDays()));
        purchase.setTax(dto.getTax() != null ? dto.getTax() : BigDecimal.ZERO);
        purchase.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO);
        purchase.setNotes(dto.getNotes());
        purchase.setStatus(PurchaseStatus.ORDERED);

        for (PurchaseCreateDto.PurchaseItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            PurchaseItem item = new PurchaseItem(purchase, product, itemDto.getQuantity(), itemDto.getUnitPrice());
            purchase.addItem(item);
        }

        Purchase saved = purchaseRepository.save(purchase);
        auditLogService.logAction(user, "CREATE_PURCHASE", "Purchase", saved.getId(),
                "Created purchase order " + saved.getOrderNumber() + " with " + saved.getItems().size() + " items", null);

        return saved;
    }

    @Transactional
    public Purchase updateStatus(Long id, PurchaseStatus newStatus, User user) {
        Purchase purchase = getPurchaseById(id);

        if (purchase.getStatus() == PurchaseStatus.RECEIVED) {
            throw new BadRequestException("Purchase order is already received and stock has been incremented.");
        }

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new BadRequestException("Cannot update status of a cancelled purchase order.");
        }

        purchase.setStatus(newStatus);

        if (newStatus == PurchaseStatus.RECEIVED) {
            LocalDate today = LocalDate.now();
            purchase.setActualDeliveryDate(today);

            // Automatically increase inventory when stock is received
            for (PurchaseItem item : purchase.getItems()) {
                Product product = item.getProduct();
                int updatedStock = (product.getCurrentStock() != null ? product.getCurrentStock() : 0) + item.getQuantity();
                product.setCurrentStock(updatedStock);
                product.updateStatus();
                productRepository.save(product);

                inventoryService.recordStockMovement(
                        product,
                        item.getQuantity(),
                        StockTransactionType.PURCHASE,
                        purchase.getOrderNumber(),
                        "Received from Purchase Order " + purchase.getOrderNumber(),
                        user
                );
            }

            // Update Supplier Metrics
            Supplier supplier = purchase.getSupplier();
            supplier.setTotalOrders((supplier.getTotalOrders() != null ? supplier.getTotalOrders() : 0) + 1);
            supplier.setTotalPurchases((supplier.getTotalPurchases() != null ? supplier.getTotalPurchases() : BigDecimal.ZERO).add(purchase.getTotalCost()));

            if (purchase.getExpectedDeliveryDate() != null && today.isAfter(purchase.getExpectedDeliveryDate())) {
                supplier.setDelayedOrders((supplier.getDelayedOrders() != null ? supplier.getDelayedOrders() : 0) + 1);
            }
            supplierRepository.save(supplier);
        }

        Purchase saved = purchaseRepository.save(purchase);
        auditLogService.logAction(user, "UPDATE_PURCHASE_STATUS", "Purchase", saved.getId(),
                "Updated purchase order " + saved.getOrderNumber() + " status to " + newStatus, null);

        return saved;
    }
}
