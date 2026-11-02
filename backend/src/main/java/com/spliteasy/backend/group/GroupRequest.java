package com.spliteasy.backend.group;

import jakarta.validation.constraints.NotBlank;

public record GroupRequest(
        @NotBlank(message = "Group ka naam required hai") String name,
        String description
) {}

