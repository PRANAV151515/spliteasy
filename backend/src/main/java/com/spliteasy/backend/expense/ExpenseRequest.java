package com.spliteasy.backend.expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseRequest(
        @NotBlank(message = "Description required hai") String description,
        @NotNull(message = "Amount required hai")
        @DecimalMin(value = "0.01", message = "Amount 0 se zyada hona chahiye") BigDecimal amount,
        @NotNull(message = "paidBy (payer ka userId) required hai") Long paidBy,
        @NotNull(message = "splitType required hai") SplitType splitType,
        LocalDate expenseDate,
        @Valid List<SplitInput> splits
) {}

