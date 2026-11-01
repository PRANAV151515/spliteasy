package com.spliteasy.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name required hai") String name,
        @Email(message = "Email sahi daal") @NotBlank(message = "Email required hai") String email,
        @NotBlank(message = "Password required hai")
        @Size(min = 6, message = "Password kam se kam 6 characters ka hona chahiye") String password
) {}

