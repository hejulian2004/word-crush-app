package com.wordcrush.server.module.admin.response;

import java.time.LocalDateTime;

public record AdminWordResponse(
        Integer id,
        String english,
        String pronunciation,
        String chinese,
        Long contentVersion,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
