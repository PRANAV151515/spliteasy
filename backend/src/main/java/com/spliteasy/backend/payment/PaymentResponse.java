package com.spliteasy.backend.payment;

import com.spliteasy.backend.user.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, UserResponse from, UserResponse to, BigDecimal amount, LocalDateTime createdAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                UserResponse.from(p.getFromUser()),
                UserResponse.from(p.getToUser()),
                p.getAmount(),
                p.getCreatedAt()
        );
    }
}