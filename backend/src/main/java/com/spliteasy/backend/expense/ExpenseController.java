package com.spliteasy.backend.expense;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> add(@PathVariable Long groupId,
                                               @Valid @RequestBody ExpenseRequest request,
                                               Authentication authentication) {
        return ResponseEntity.ok(expenseService.addExpense(groupId, request, authentication.getName()));
    }

    @GetMapping
    public List<ExpenseResponse> list(@PathVariable Long groupId,
                                      Authentication authentication) {
        return expenseService.getGroupExpenses(groupId, authentication.getName());
    }
}


