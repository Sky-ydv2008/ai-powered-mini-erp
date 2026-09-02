package com.example.intellierp.service;

import com.example.intellierp.entity.Customer;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.CustomerTier;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.CustomerRepository;
import com.example.intellierp.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<Customer> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        customers.forEach(this::evaluateTier);
        return customers;
    }

    public Customer getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        evaluateTier(customer);
        return customer;
    }

    @Transactional
    public Customer createCustomer(Customer customer, User user) {
        evaluateTier(customer);
        Customer saved = customerRepository.save(customer);
        auditLogService.logAction(user, "CREATE_CUSTOMER", "Customer", saved.getId(),
                "Created customer " + saved.getName(), null);
        return saved;
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer details, User user) {
        Customer customer = getCustomerById(id);
        customer.setName(details.getName());
        customer.setEmail(details.getEmail());
        customer.setPhone(details.getPhone());
        customer.setAddress(details.getAddress());
        if (details.getTier() != null) {
            customer.setTier(details.getTier());
        } else {
            evaluateTier(customer);
        }

        Customer saved = customerRepository.save(customer);
        auditLogService.logAction(user, "UPDATE_CUSTOMER", "Customer", saved.getId(),
                "Updated customer " + saved.getName(), null);
        return saved;
    }

    @Transactional
    public void deleteCustomer(Long id, User user) {
        Customer customer = getCustomerById(id);
        if (!saleRepository.findByCustomerId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete customer '" + customer.getName() + "' because sales transactions exist for this customer.");
        }
        customerRepository.delete(customer);
        auditLogService.logAction(user, "DELETE_CUSTOMER", "Customer", id,
                "Deleted customer " + customer.getName(), null);
    }

    public void evaluateTier(Customer customer) {
        if (customer == null) return;

        BigDecimal spend = customer.getTotalSpend() != null ? customer.getTotalSpend() : BigDecimal.ZERO;
        int orders = customer.getTotalOrders() != null ? customer.getTotalOrders() : 0;
        LocalDateTime last = customer.getLastPurchaseDate();

        if (last != null && last.isBefore(LocalDateTime.now().minusDays(45))) {
            customer.setTier(CustomerTier.CHURN_RISK);
        } else if (spend.compareTo(new BigDecimal("50000.00")) >= 0 || orders >= 10) {
            customer.setTier(CustomerTier.VIP);
        } else if (orders >= 2) {
            customer.setTier(CustomerTier.RETURNING);
        } else {
            customer.setTier(CustomerTier.REGULAR);
        }
    }

    public Map<String, Object> getCustomerAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalCustomers", customerRepository.count());
        analytics.put("vipCustomers", customerRepository.countByTier(CustomerTier.VIP));
        analytics.put("returningCustomers", customerRepository.countByTier(CustomerTier.RETURNING));
        analytics.put("regularCustomers", customerRepository.countByTier(CustomerTier.REGULAR));
        analytics.put("churnRiskCustomers", customerRepository.countByTier(CustomerTier.CHURN_RISK));
        analytics.put("totalCustomerSpend", customerRepository.sumTotalCustomerSpend());
        analytics.put("topCustomers", customerRepository.findTop10ByOrderByTotalSpendDesc());
        return analytics;
    }
}
