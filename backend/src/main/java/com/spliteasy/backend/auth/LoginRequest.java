package com.spliteasy.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email required hai") String email,
        @NotBlank(message = "Password required hai") String password
) {}

