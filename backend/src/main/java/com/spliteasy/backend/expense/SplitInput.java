package com.spliteasy.backend.expense;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SplitInput(
        @NotNull(message = "User is required") Long userId,
        BigDecimal value
) {}
