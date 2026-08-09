package com.wordcrush.server.module.admin.response;

public record AdminOverviewResponse(
        long totalUsers,
        long activeUsers,
        long totalWords,
        long activeWords
) {
}
