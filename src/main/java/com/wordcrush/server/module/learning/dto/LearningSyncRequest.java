package com.wordcrush.server.module.learning.dto;

import java.util.List;

public record LearningSyncRequest(List<LearningMutationRequest> mutations) {
}
