package com.spliteasy.backend.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SettleRequest(
        @NotNull(message = "Payer is required") Long fromUserId,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount
) {}