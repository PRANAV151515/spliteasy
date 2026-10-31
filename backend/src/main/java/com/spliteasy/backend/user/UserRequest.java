package com.spliteasy.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "Name required hai") String name,
        @Email(message = "Email sahi daal") @NotBlank String email
) {}

