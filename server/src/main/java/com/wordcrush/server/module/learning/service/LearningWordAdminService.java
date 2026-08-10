package com.wordcrush.server.module.learning.service;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.module.learning.api.LearningWordAdminFacade;
import com.wordcrush.server.module.learning.entity.LearningWord;
import com.wordcrush.server.module.learning.repository.LearningWordRepository;
import com.wordcrush.server.module.user.api.UserAdminFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningWordAdminService implements LearningWordAdminFacade {

    private static final int ACTIVE_STATUS = 1;

    private final LearningWordRepository learningWordRepository;
    private final UserAdminFacade userAdminFacade;

    @Override
    @Transactional(readOnly = true)
    public WordSummary summary(Long operatorId) {
        requireAdmin(operatorId);
        List<LearningWord> words = learningWordRepository.findAll();
        return new WordSummary(
                words.size(),
                words.stream().filter(this::isActive).count()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WordPage listWords(Long operatorId, String query, Integer status, Integer page, Integer size) {
        requireAdmin(operatorId);
        String normalizedQuery = normalize(query);
        List<WordView> filtered = learningWordRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .filter(word -> normalizedQuery.isBlank()
                        || word.getEnglish().toLowerCase().contains(normalizedQuery)
                        || word.getChinese().toLowerCase().contains(normalizedQuery)
                        || word.getPronunciation().toLowerCase().contains(normalizedQuery))
                .filter(word -> status == null || status.equals(word.getStatus()))
                .map(this::toView)
                .toList();
        return page(filtered, page, size);
    }

    @Override
    @Transactional
    public WordView createWord(
            Long operatorId,
            Integer id,
            String english,
            String pronunciation,
            String chinese
    ) {
        requireAdmin(operatorId);
        if (id == null || id <= 0 || learningWordRepository.existsById(id)) {
            throw new BusinessException(ApiCode.CONFLICT, "word id already exists or is invalid");
        }
        LearningWord word = new LearningWord();
        word.setId(id);
        word.setEnglish(english.trim());
        word.setPronunciation(pronunciation.trim());
        word.setChinese(chinese.trim());
        word.setContentVersion(1L);
        word.setStatus(ACTIVE_STATUS);
        return toView(learningWordRepository.save(word));
    }

    @Override
    @Transactional
    public WordView updateWord(
            Long operatorId,
            Integer id,
            String english,
            String pronunciation,
            String chinese,
            Integer status
    ) {
        requireAdmin(operatorId);
        LearningWord word = findWord(id);
        int nextStatus = normalizeStatus(status);
        String nextEnglish = english.trim();
        String nextPronunciation = pronunciation.trim();
        String nextChinese = chinese.trim();
        boolean contentChanged = !nextEnglish.equals(word.getEnglish())
                || !nextPronunciation.equals(word.getPronunciation())
                || !nextChinese.equals(word.getChinese());
        word.setEnglish(nextEnglish);
        word.setPronunciation(nextPronunciation);
        word.setChinese(nextChinese);
        word.setStatus(nextStatus);
        if (contentChanged) {
            word.setContentVersion((word.getContentVersion() == null ? 0L : word.getContentVersion()) + 1);
        }
        return toView(learningWordRepository.save(word));
    }

    @Override
    @Transactional
    public WordImportResult importWords(
            Long operatorId,
            List<WordImportRow> rows,
            boolean replace,
            int skipped
    ) {
        requireAdmin(operatorId);
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "csv file contains no words");
        }

        List<LearningWord> existingWords = learningWordRepository.findAll();
        java.util.Map<Integer, LearningWord> existing = existingWords.stream()
                .collect(java.util.stream.Collectors.toMap(LearningWord::getId, word -> word));
        java.util.Set<Integer> incomingIds = rows.stream()
                .map(WordImportRow::id)
                .collect(java.util.stream.Collectors.toSet());
        int added = 0;
        int updated = 0;
        int disabled = 0;

        for (WordImportRow row : rows) {
            LearningWord word = existing.get(row.id());
            if (word == null) {
                word = new LearningWord();
                word.setId(row.id());
                word.setContentVersion(1L);
                added++;
            } else if (!row.english().equals(word.getEnglish())
                    || !row.pronunciation().equals(word.getPronunciation())
                    || !row.chinese().equals(word.getChinese())
                    || !Integer.valueOf(ACTIVE_STATUS).equals(word.getStatus())) {
                word.setContentVersion((word.getContentVersion() == null ? 0L : word.getContentVersion()) + 1);
                updated++;
            }
            word.setEnglish(row.english());
            word.setPronunciation(row.pronunciation());
            word.setChinese(row.chinese());
            word.setStatus(ACTIVE_STATUS);
            existing.put(row.id(), word);
        }

        if (replace) {
            for (LearningWord word : existing.values()) {
                if (!incomingIds.contains(word.getId()) && isActive(word)) {
                    word.setStatus(0);
                    disabled++;
                }
            }
        }
        learningWordRepository.saveAll(existing.values());
        return new WordImportResult(added, updated, disabled, rows.size(), skipped);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportWords(Long operatorId) {
        requireAdmin(operatorId);
        StringBuilder csv = new StringBuilder("id,english,pronunciation,chinese\n");
        learningWordRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .filter(this::isActive)
                .forEach(word -> csv.append(word.getId())
                        .append(',').append(csvField(word.getEnglish()))
                        .append(',').append(csvField(word.getPronunciation()))
                        .append(',').append(csvField(word.getChinese()))
                        .append('\n'));
        return ("\uFEFF" + csv).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void requireAdmin(Long operatorId) {
        userAdminFacade.currentAdmin(operatorId);
    }

    private LearningWord findWord(Integer wordId) {
        return learningWordRepository.findById(wordId)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "word not found"));
    }

    private WordView toView(LearningWord word) {
        return new WordView(
                word.getId(), word.getEnglish(), word.getPronunciation(), word.getChinese(),
                word.getContentVersion(), word.getStatus(), word.getCreatedAt(), word.getUpdatedAt());
    }

    private WordPage page(List<WordView> items, Integer page, Integer size) {
        int normalizedPage = Math.max(page == null ? 0 : page, 0);
        int normalizedSize = Math.min(Math.max(size == null ? 20 : size, 1), 100);
        int from = Math.min(normalizedPage * normalizedSize, items.size());
        int to = Math.min(from + normalizedSize, items.size());
        return new WordPage(items.subList(from, to), normalizedPage, normalizedSize, items.size());
    }

    private int normalizeStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "status must be 0 or 1");
        }
        return status;
    }

    private boolean isActive(LearningWord word) {
        return Integer.valueOf(ACTIVE_STATUS).equals(word.getStatus());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String csvField(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")
                ? "\"" + escaped + "\""
                : escaped;
    }
}
