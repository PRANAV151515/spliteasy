package com.spliteasy.backend.group;

import com.spliteasy.backend.user.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record GroupResponse(
        Long id,
        String name,
        String description,
        UserResponse createdBy,
        List<UserResponse> members,
        LocalDateTime createdAt
) {

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                UserResponse.from(group.getCreatedBy()),
                group.getMembers().stream().map(UserResponse::from).toList(),
                group.getCreatedAt()
        );
    }
}

