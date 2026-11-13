package com.spliteasy.backend.group;

import jakarta.validation.constraints.NotBlank;

public record GroupRequest(
        @NotBlank(message = "Group name is required") String name,
        String description
) {}

