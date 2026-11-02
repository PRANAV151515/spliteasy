package com.spliteasy.backend.group;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(
        @NotBlank(message = "Email required hai")
        @Email(message = "Email sahi daal") String email
) {}

