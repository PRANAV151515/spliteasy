package com.spliteasy.backend.balance;

import com.spliteasy.backend.user.UserResponse;

import java.math.BigDecimal;

public record TransferResponse(UserResponse from, UserResponse to, BigDecimal amount) {}

