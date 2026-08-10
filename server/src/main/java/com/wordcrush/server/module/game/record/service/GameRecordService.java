package com.wordcrush.server.module.game.record.service;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.common.util.TimeFormats;
import com.wordcrush.server.module.game.ranking.service.RankingCacheService;
import com.wordcrush.server.module.game.record.dto.DeleteGameRecordRequest;
import com.wordcrush.server.module.game.record.dto.SaveGameRecordRequest;
import com.wordcrush.server.module.game.record.dto.UsernameRequest;
import com.wordcrush.server.module.game.record.entity.GameRecord;
import com.wordcrush.server.module.game.record.entity.GameRecordWord;
import com.wordcrush.server.module.game.record.repository.GameRecordRepository;
import com.wordcrush.server.module.game.record.response.GameRecordResponse;
import com.wordcrush.server.module.game.api.GameType;
import com.wordcrush.server.module.user.api.UserDirectory;
import com.wordcrush.server.module.user.api.UserSnapshot;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GameRecordService {

    private final GameRecordRepository gameRecordRepository;
    private final UserDirectory userDirectory;
    private final RankingCacheService rankingCacheService;

    @Transactional
    public void addGameRecord(SaveGameRecordRequest request) {
        if (request == null) {
            throw new BusinessException("request body must not be null");
        }
        validateUsername(request.username());
        GameType.fromCode(request.gameType());
        if (request.score() == null || request.score() < 0) {
            throw new BusinessException("score must be >= 0");
        }
        if (!StringUtils.hasText(request.time())) {
            throw new BusinessException("time must not be blank");
        }

        UserSnapshot user = loadUser(request.username());
        LocalDateTime playedAt = TimeFormats.parseGameTime(request.time());
        if (gameRecordRepository.existsByUserIdAndGameTypeAndScoreAndPlayedAt(
                user.id(), request.gameType(), request.score(), playedAt)) {
            return;
        }

        GameRecord record = new GameRecord();
        record.setUserId(user.id());
        record.setGameType(request.gameType());
        record.setScore(request.score());
        record.setPlayedAt(playedAt);
        record.replaceLearnedWords(sanitizeLearnedWords(request.learnedWords()));
        gameRecordRepository.save(record);
        rankingCacheService.evictGameType(request.gameType());
    }

    @Transactional
    public void deleteGameRecord(DeleteGameRecordRequest request) {
        if (request == null) {
            throw new BusinessException("request body must not be null");
        }
        validateUsername(request.username());
        GameType.fromCode(request.gameType());
        if (request.score() == null || request.score() < 0) {
            throw new BusinessException("score must be >= 0");
        }
        if (!StringUtils.hasText(request.time())) {
            throw new BusinessException("time must not be blank");
        }

        UserSnapshot user = loadUser(request.username());
        LocalDateTime playedAt = TimeFormats.parseGameTime(request.time());
        GameRecord record = gameRecordRepository
                .findFirstByUserIdAndGameTypeAndScoreAndPlayedAt(
                        user.id(), request.gameType(), request.score(), playedAt)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "game record not found"));
        gameRecordRepository.delete(record);
        rankingCacheService.evictGameType(request.gameType());
    }

    @Transactional(readOnly = true)
    public List<GameRecordResponse> getAllGameRecords(UsernameRequest request) {
        if (request == null) {
            throw new BusinessException("request body must not be null");
        }
        validateUsername(request.username());
        UserSnapshot user = loadUser(request.username());
        return gameRecordRepository.findByUserIdOrderByPlayedAtDesc(user.id())
                .stream()
                .map(record -> new GameRecordResponse(
                        user.username(),
                        record.getGameType(),
                        record.getScore(),
                        TimeFormats.formatGameTime(record.getPlayedAt()),
                        record.getLearnedWords().stream().map(GameRecordWord::getWordContent).toList()
                ))
                .toList();
    }

    private UserSnapshot loadUser(String username) {
        return userDirectory.requireUserByUsername(username);
    }

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("username must not be blank");
        }
    }

    private List<String> sanitizeLearnedWords(List<String> learnedWords) {
        if (CollectionUtils.isEmpty(learnedWords)) {
            return List.of();
        }
        return learnedWords.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }
}
