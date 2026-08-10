package com.wordcrush.server.module.learning.service;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.module.learning.dto.DailyTargetRequest;
import com.wordcrush.server.module.learning.dto.LearningMutationRequest;
import com.wordcrush.server.module.learning.dto.LearningOperation;
import com.wordcrush.server.module.learning.dto.LearningSyncRequest;
import com.wordcrush.server.module.learning.entity.LearningSyncMutation;
import com.wordcrush.server.module.learning.entity.LearningWord;
import com.wordcrush.server.module.learning.entity.UserDailyPlan;
import com.wordcrush.server.module.learning.entity.UserDailyPlanItem;
import com.wordcrush.server.module.learning.entity.UserLearningSettings;
import com.wordcrush.server.module.learning.entity.UserWordProgress;
import com.wordcrush.server.module.learning.repository.LearningSyncMutationRepository;
import com.wordcrush.server.module.learning.repository.LearningWordRepository;
import com.wordcrush.server.module.learning.repository.UserDailyPlanRepository;
import com.wordcrush.server.module.learning.repository.UserLearningSettingsRepository;
import com.wordcrush.server.module.learning.repository.UserWordProgressRepository;
import com.wordcrush.server.module.learning.response.CatalogResponse;
import com.wordcrush.server.module.learning.response.LearningStateResponse;
import com.wordcrush.server.module.learning.response.LearningSyncResponse;
import com.wordcrush.server.module.learning.response.ProgressResponse;
import com.wordcrush.server.module.learning.response.WordResponse;
import com.wordcrush.server.module.user.api.UserDirectory;
import com.wordcrush.server.security.api.TokenSession;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LearningService {

    public static final int DEFAULT_DAILY_TARGET = 30;
    private static final int MAX_DAILY_TARGET = 500;
    private static final int MAX_SYNC_MUTATIONS = 200;
    private static final int ACTIVE_STATUS = 1;

    private final LearningWordRepository learningWordRepository;
    private final UserWordProgressRepository progressRepository;
    private final UserLearningSettingsRepository settingsRepository;
    private final UserDailyPlanRepository dailyPlanRepository;
    private final LearningSyncMutationRepository mutationRepository;
    private final UserDirectory userDirectory;

    @Transactional(readOnly = true)
    public CatalogResponse getCatalog(
            TokenSession session,
            String query,
            Boolean mastered,
            List<Integer> ids,
            Integer page,
            Integer size
    ) {
        long userId = loadUserId(session);
        int normalizedPage = Math.max(page == null ? 0 : page, 0);
        int normalizedSize = Math.min(Math.max(size == null ? 100 : size, 1), 1000);
        List<LearningWord> words = ids == null || ids.isEmpty()
                ? learningWordRepository.findAllByStatusOrderByIdAsc(ACTIVE_STATUS)
                : learningWordRepository.findAllByIdInAndStatusOrderByIdAsc(ids, ACTIVE_STATUS);
        Map<Integer, UserWordProgress> progress = progressMap(userId, words.stream()
                .map(LearningWord::getId)
                .toList());
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        List<LearningWord> filtered = words.stream()
                .filter(word -> normalizedQuery.isBlank()
                        || word.getEnglish().toLowerCase().contains(normalizedQuery)
                        || word.getChinese().toLowerCase().contains(normalizedQuery)
                        || word.getPronunciation().toLowerCase().contains(normalizedQuery))
                .filter(word -> {
                    if (mastered == null) {
                        return true;
                    }
                    UserWordProgress wordProgress = progress.get(word.getId());
                    boolean wordMastered = wordProgress != null && Boolean.TRUE.equals(wordProgress.getMastered());
                    return wordMastered == mastered;
                })
                .toList();
        int from = Math.min(normalizedPage * normalizedSize, filtered.size());
        int to = Math.min(from + normalizedSize, filtered.size());
        List<WordResponse> items = filtered.subList(from, to).stream()
                .map(word -> toWordResponse(word, progress.get(word.getId())))
                .toList();
        return new CatalogResponse(items, normalizedPage, normalizedSize, filtered.size(), 1L);
    }

    @Transactional
    public LearningStateResponse getState(TokenSession session) {
        long userId = loadUserId(session);
        UserLearningSettings settings = getOrCreateSettings(userId);
        UserDailyPlan plan = ensureDailyPlan(userId, settings.getDailyTarget());
        return buildState(userId, settings.getDailyTarget(), plan);
    }

    @Transactional
    public LearningStateResponse updateDailyTarget(TokenSession session, DailyTargetRequest request) {
        long userId = loadUserId(session);
        int target = validateDailyTarget(request == null ? null : request.dailyTarget());
        UserLearningSettings settings = getOrCreateSettings(userId);
        settings.setDailyTarget(target);
        settingsRepository.save(settings);
        UserDailyPlan plan = ensureDailyPlan(userId, target);
        return buildState(userId, target, plan);
    }

    @Transactional
    public LearningSyncResponse sync(TokenSession session, LearningSyncRequest request) {
        long userId = loadUserId(session);
        List<LearningMutationRequest> mutations = request == null || request.mutations() == null
                ? List.of()
                : request.mutations();
        if (mutations.size() > MAX_SYNC_MUTATIONS) {
            throw new BusinessException("too many learning mutations");
        }

        List<String> accepted = new ArrayList<>();
        for (LearningMutationRequest mutation : mutations) {
            validateMutation(mutation);
            if (mutationRepository.findByUserIdAndMutationId(userId, mutation.mutationId()).isPresent()) {
                accepted.add(mutation.mutationId());
                continue;
            }

            switch (mutation.operation()) {
                case UPDATE_DAILY_TARGET -> updateTargetFromMutation(userId, mutation);
                case CORRECT_MATCH, MARK_UNREMEMBERED, IMPORT_SNAPSHOT ->
                        applyWordMutation(userId, mutation);
            }
            saveMutation(userId, mutation);
            accepted.add(mutation.mutationId());
        }

        UserLearningSettings settings = getOrCreateSettings(userId);
        UserDailyPlan plan = ensureDailyPlan(userId, settings.getDailyTarget());
        return new LearningSyncResponse(accepted, buildState(userId, settings.getDailyTarget(), plan));
    }

    private void validateMutation(LearningMutationRequest mutation) {
        if (mutation == null || !StringUtils.hasText(mutation.mutationId())) {
            throw new BusinessException("mutationId must not be blank");
        }
        if (mutation.mutationId().length() > 96) {
            throw new BusinessException("mutationId is too long");
        }
        if (mutation.operation() == null) {
            throw new BusinessException("operation must not be null");
        }
        if (mutation.operation() == LearningOperation.UPDATE_DAILY_TARGET) {
            validateDailyTarget(mutation.dailyTarget());
            return;
        }
        if (mutation.wordId() == null) {
            throw new BusinessException("wordId must not be null");
        }
        if (mutation.operation() == LearningOperation.IMPORT_SNAPSHOT
                && (mutation.masterCount() == null || mutation.masterCount() < 0 || mutation.masterCount() > 3)) {
            throw new BusinessException("masterCount must be between 0 and 3");
        }
    }

    private void updateTargetFromMutation(long userId, LearningMutationRequest mutation) {
        UserLearningSettings settings = getOrCreateSettings(userId);
        settings.setDailyTarget(validateDailyTarget(mutation.dailyTarget()));
        settingsRepository.save(settings);
    }

    private void applyWordMutation(long userId, LearningMutationRequest mutation) {
        LearningWord word = learningWordRepository.findById(mutation.wordId())
                .filter(item -> item.getStatus() == ACTIVE_STATUS)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "word not found"));
        UserWordProgress progress = progressRepository.findByUserIdAndWord_Id(userId, word.getId())
                .orElseGet(() -> newProgress(userId, word));
        int current = progress.getMasterCount() == null ? 0 : progress.getMasterCount();
        int next = switch (mutation.operation()) {
            case CORRECT_MATCH -> Math.min(3, current + 1);
            case MARK_UNREMEMBERED -> 0;
            case IMPORT_SNAPSHOT -> Math.max(current, mutation.masterCount());
            default -> current;
        };
        progress.setMasterCount(next);
        progress.setMastered(next >= 3);
        progress.setVersion((progress.getVersion() == null ? 0L : progress.getVersion()) + 1L);
        progressRepository.save(progress);
    }

    private UserWordProgress newProgress(long userId, LearningWord word) {
        UserWordProgress progress = new UserWordProgress();
        progress.setUserId(userId);
        progress.setWord(word);
        progress.setMasterCount(0);
        progress.setMastered(false);
        progress.setVersion(0L);
        return progress;
    }

    private void saveMutation(long userId, LearningMutationRequest request) {
        LearningSyncMutation mutation = new LearningSyncMutation();
        mutation.setUserId(userId);
        mutation.setMutationId(request.mutationId());
        if (request.wordId() != null) {
            mutation.setWord(learningWordRepository.findById(request.wordId()).orElse(null));
        }
        mutation.setOperation(request.operation().name());
        mutation.setMasterCount(request.masterCount());
        mutation.setDailyTarget(request.dailyTarget());
        mutation.setClientAt(request.clientAt());
        mutationRepository.save(mutation);
    }

    private UserDailyPlan ensureDailyPlan(long userId, int dailyTarget) {
        LocalDate today = LocalDate.now();
        UserDailyPlan plan = dailyPlanRepository.findByUserIdAndPlanDate(userId, today)
                .orElseGet(() -> {
                    UserDailyPlan created = new UserDailyPlan();
                    created.setUserId(userId);
                    created.setPlanDate(today);
                    created.setDailyTarget(dailyTarget);
                    created.replaceItems(selectUnmasteredWords(userId, dailyTarget, Set.of()));
                    return dailyPlanRepository.save(created);
                });
        if (plan.getDailyTarget() == null || plan.getDailyTarget() < dailyTarget) {
            List<LearningWord> existing = plan.getItems().stream()
                    .map(UserDailyPlanItem::getWord)
                    .toList();
            Set<Integer> existingIds = existing.stream().map(LearningWord::getId).collect(Collectors.toSet());
            List<LearningWord> additional = selectUnmasteredWords(userId, dailyTarget, existingIds);
            if (!additional.isEmpty()) {
                List<LearningWord> all = new ArrayList<>(existing);
                all.addAll(additional);
                plan.replaceItems(all);
            }
            plan.setDailyTarget(dailyTarget);
            dailyPlanRepository.save(plan);
        }
        return plan;
    }

    private List<LearningWord> selectUnmasteredWords(Long userId, int target, Set<Integer> excludedIds) {
        Map<Integer, UserWordProgress> progress = progressMap(userId, List.of());
        return learningWordRepository.findAllByStatusOrderByIdAsc(ACTIVE_STATUS).stream()
                .filter(word -> !excludedIds.contains(word.getId()))
                .filter(word -> {
                    UserWordProgress wordProgress = progress.get(word.getId());
                    return wordProgress == null || !Boolean.TRUE.equals(wordProgress.getMastered());
                })
                .limit(Math.max(target, 1))
                .toList();
    }

    private LearningStateResponse buildState(long userId, int dailyTarget, UserDailyPlan plan) {
        List<UserWordProgress> allProgress = progressRepository.findByUserId(userId);
        Map<Integer, UserWordProgress> progressMap = allProgress.stream()
                .collect(Collectors.toMap(item -> item.getWord().getId(), item -> item, (left, right) -> right));
        List<LearningWord> allWords = learningWordRepository.findAllByStatusOrderByIdAsc(ACTIVE_STATUS);
        int availableUnmastered = (int) allWords.stream()
                .filter(word -> {
                    UserWordProgress progress = progressMap.get(word.getId());
                    return progress == null || !Boolean.TRUE.equals(progress.getMastered());
                })
                .count();
        List<Integer> todayIds = plan.getItems().stream().map(item -> item.getWord().getId()).toList();
        List<WordResponse> todayWords = plan.getItems().stream()
                .map(item -> toWordResponse(item.getWord(), progressMap.get(item.getWord().getId())))
                .toList();
        int completed = (int) todayWords.stream().filter(word -> Boolean.TRUE.equals(word.mastered())).count();
        boolean dailyCompleted = todayWords.isEmpty() || completed == todayWords.size();
        long syncVersion = allProgress.stream()
                .map(UserWordProgress::getVersion)
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(0L);
        List<ProgressResponse> progress = allProgress.stream()
                .map(this::toProgressResponse)
                .toList();
        return new LearningStateResponse(
                dailyTarget,
                plan.getPlanDate().toString(),
                todayIds,
                todayWords,
                completed,
                availableUnmastered,
                availableUnmastered == 0,
                dailyCompleted,
                dailyCompleted && availableUnmastered > 0,
                syncVersion,
                progress
        );
    }

    private Map<Integer, UserWordProgress> progressMap(Long userId, Collection<Integer> wordIds) {
        List<UserWordProgress> progress = wordIds.isEmpty()
                ? progressRepository.findByUserId(userId)
                : progressRepository.findByUserIdAndWord_IdIn(userId, wordIds);
        return progress.stream().collect(Collectors.toMap(
                item -> item.getWord().getId(),
                item -> item,
                (left, right) -> right,
                HashMap::new
        ));
    }

    private WordResponse toWordResponse(LearningWord word, UserWordProgress progress) {
        return new WordResponse(
                word.getId(),
                word.getEnglish(),
                word.getPronunciation(),
                word.getChinese(),
                word.getContentVersion(),
                progress == null || progress.getMasterCount() == null ? 0 : progress.getMasterCount(),
                progress != null && Boolean.TRUE.equals(progress.getMastered())
        );
    }

    private ProgressResponse toProgressResponse(UserWordProgress progress) {
        return new ProgressResponse(
                progress.getWord().getId(),
                progress.getMasterCount(),
                progress.getMastered(),
                progress.getVersion(),
                progress.getUpdatedAt() == null
                        ? null
                        : progress.getUpdatedAt().toInstant(ZoneOffset.UTC).toString()
        );
    }

    private UserLearningSettings getOrCreateSettings(long userId) {
        return settingsRepository.findByUserId(userId).orElseGet(() -> {
            UserLearningSettings settings = new UserLearningSettings();
            settings.setUserId(userId);
            settings.setDailyTarget(DEFAULT_DAILY_TARGET);
            return settingsRepository.save(settings);
        });
    }

    private int validateDailyTarget(Integer target) {
        if (target == null || target < 1 || target > MAX_DAILY_TARGET) {
            throw new BusinessException("dailyTarget must be between 1 and " + MAX_DAILY_TARGET);
        }
        return target;
    }

    private long loadUserId(TokenSession session) {
        if (session == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "invalid token");
        }
        return userDirectory.requireUser(session.userId()).id();
    }
}
