package com.example.intellierp.service;

import com.example.intellierp.entity.Expense;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.ExpenseCategory;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Page<Expense> getExpensesPaged(ExpenseCategory category, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now().plusDays(1);
        return expenseRepository.filterExpenses(category, start, end, pageable);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Transactional
    public Expense createExpense(Expense expense, User user) {
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Expense amount must be positive");
        }
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
        }
        expense.setRecordedBy(user);
        Expense saved = expenseRepository.save(expense);

        auditLogService.logAction(user, "CREATE_EXPENSE", "Expense", saved.getId(),
                "Recorded expense: ₹" + saved.getAmount() + " for " + saved.getCategory(), null);

        return saved;
    }

    @Transactional
    public Expense updateExpense(Long id, Expense details, User user) {
        Expense expense = getExpenseById(id);
        if (details.getAmount() != null && details.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Expense amount must be positive");
        }
        expense.setCategory(details.getCategory());
        expense.setAmount(details.getAmount());
        expense.setExpenseDate(details.getExpenseDate());
        expense.setDescription(details.getDescription());
        expense.setPaymentMethod(details.getPaymentMethod());

        Expense saved = expenseRepository.save(expense);
        auditLogService.logAction(user, "UPDATE_EXPENSE", "Expense", saved.getId(),
                "Updated expense ID " + saved.getId(), null);

        return saved;
    }

    @Transactional
    public void deleteExpense(Long id, User user) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
        auditLogService.logAction(user, "DELETE_EXPENSE", "Expense", id,
                "Deleted expense of ₹" + expense.getAmount() + " (" + expense.getCategory() + ")", null);
    }

    public List<Map<String, Object>> getCategoryBreakdown(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        List<Object[]> rows = expenseRepository.sumExpensesByCategoryBetween(start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("category", r[0]);
            map.put("amount", r[1]);
            result.add(map);
        }
        return result;
    }

    public BigDecimal getTotalExpensesBetween(LocalDate start, LocalDate end) {
        return expenseRepository.sumExpensesBetween(start, end);
    }
}
