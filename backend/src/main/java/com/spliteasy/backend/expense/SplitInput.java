package com.spliteasy.backend.expense;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SplitInput(
        @NotNull(message = "userId required hai") Long userId,
        BigDecimal value
) {}
