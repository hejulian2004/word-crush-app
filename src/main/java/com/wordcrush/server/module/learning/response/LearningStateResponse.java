package com.wordcrush.server.module.learning.response;

import java.util.List;

public record LearningStateResponse(
        Integer dailyTarget,
        String planDate,
        List<Integer> todayWordIds,
        List<WordResponse> todayWords,
        int completedCount,
        int availableUnmasteredCount,
        boolean allWordsMastered,
        boolean dailyCompleted,
        boolean canIncreaseDailyTarget,
        long syncVersion,
        List<ProgressResponse> progress
) {
}
