package com.wordcrush.server.module.learning.dto;

public record LearningMutationRequest(
        String mutationId,
        Integer wordId,
        LearningOperation operation,
        Integer masterCount,
        Integer dailyTarget,
        String clientAt
) {
}
