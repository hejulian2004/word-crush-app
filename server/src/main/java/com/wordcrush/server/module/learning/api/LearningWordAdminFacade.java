package com.wordcrush.server.module.learning.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Administrative word-catalog use cases exposed to the admin module.
 */
public interface LearningWordAdminFacade {

    WordSummary summary(Long operatorId);

    WordPage listWords(Long operatorId, String query, Integer status, Integer page, Integer size);

    WordView createWord(Long operatorId, Integer id, String english, String pronunciation, String chinese);

    WordView updateWord(
            Long operatorId,
            Integer id,
            String english,
            String pronunciation,
            String chinese,
            Integer status
    );

    WordImportResult importWords(Long operatorId, List<WordImportRow> words, boolean replace, int skipped);

    byte[] exportWords(Long operatorId);

    record WordView(
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

    record WordPage(
            List<WordView> items,
            int page,
            int size,
            long total
    ) {
    }

    record WordSummary(
            long totalWords,
            long activeWords
    ) {
    }

    record WordImportRow(
            Integer id,
            String english,
            String pronunciation,
            String chinese
    ) {
    }

    record WordImportResult(
            int added,
            int updated,
            int disabled,
            int total,
            int skipped
    ) {
    }
}
