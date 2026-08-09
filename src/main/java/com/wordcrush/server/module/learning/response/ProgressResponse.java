package com.wordcrush.server.module.learning.response;

public record ProgressResponse(
        Integer wordId,
        Integer masterCount,
        Boolean mastered,
        Long version,
        String updatedAt
) {
}
