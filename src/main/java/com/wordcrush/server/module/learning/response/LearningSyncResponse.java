package com.wordcrush.server.module.learning.response;

import java.util.List;

public record LearningSyncResponse(
        List<String> acceptedMutationIds,
        LearningStateResponse state
) {
}
