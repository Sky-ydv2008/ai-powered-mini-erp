package com.example.intellierp.controller;

import com.example.intellierp.entity.Expense;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.ExpenseCategory;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expenses", description = "Operational Expense Management & Budget Tracking")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping
    @Operation(summary = "Get all expenses")
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get paginated expenses", description = "Filter by category and date range")
    public ResponseEntity<Page<Expense>> getExpensesPaged(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(expenseService.getExpensesPaged(category, startDate, endDate,
                PageRequest.of(page, size, Sort.by("expenseDate").descending())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/breakdown")
    @Operation(summary = "Get expense breakdown by category")
    public ResponseEntity<List<Map<String, Object>>> getExpenseBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getCategoryBreakdown(startDate, endDate));
    }

    @PostMapping
    @Operation(summary = "Create new expense")
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody Expense expense, Authentication auth) {
        return ResponseEntity.ok(expenseService.createExpense(expense, getCurrentUser(auth)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense details")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @Valid @RequestBody Expense expense, Authentication auth) {
        return ResponseEntity.ok(expenseService.updateExpense(id, expense, getCurrentUser(auth)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Authentication auth) {
        expenseService.deleteExpense(id, getCurrentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
