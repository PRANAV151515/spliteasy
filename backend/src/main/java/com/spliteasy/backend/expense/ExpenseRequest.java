package com.spliteasy.backend.expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseRequest(
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount,
        @NotNull(message = "Payer is required") Long paidBy,
        @NotNull(message = "Split type is required") SplitType splitType,
        LocalDate expenseDate,
        @Valid List<SplitInput> splits
) {}

