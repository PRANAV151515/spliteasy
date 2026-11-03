package com.spliteasy.backend.expense;

import com.spliteasy.backend.user.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        UserResponse paidBy,
        SplitType splitType,
        LocalDate expenseDate,
        List<SplitResponse> splits
) {

    public record SplitResponse(UserResponse user, BigDecimal amount) {}

    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getDescription(),
                e.getAmount(),
                UserResponse.from(e.getPaidBy()),
                e.getSplitType(),
                e.getExpenseDate(),
                e.getSplits().stream()
                        .map(s -> new SplitResponse(UserResponse.from(s.getUser()), s.getAmount()))
                        .toList()
        );
    }
}

