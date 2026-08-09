package com.wordcrush.server.module.admin.response;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String role,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
