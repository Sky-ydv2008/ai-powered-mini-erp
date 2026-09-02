package com.example.intellierp.controller;

import com.example.intellierp.entity.Customer;
import com.example.intellierp.entity.User;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer CRM, Tiering & Retention Analytics")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get CRM analytics (VIP, Churn Risk, Spending metrics)")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics() {
        return ResponseEntity.ok(customerService.getCustomerAnalytics());
    }

    @PostMapping
    @Operation(summary = "Create customer")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer, Authentication auth) {
        return ResponseEntity.ok(customerService.createCustomer(customer, getCurrentUser(auth)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer, Authentication auth) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer, getCurrentUser(auth)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id, Authentication auth) {
        customerService.deleteCustomer(id, getCurrentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
