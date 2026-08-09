package com.wordcrush.server.module.game.ranking.service;

import com.wordcrush.server.common.constant.GameType;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.common.util.TimeFormats;
import com.wordcrush.server.module.game.ranking.dto.RankingRequest;
import com.wordcrush.server.module.game.ranking.response.RankingItemResponse;
import com.wordcrush.server.module.game.record.entity.GameRecord;
import com.wordcrush.server.module.game.record.repository.GameRecordRepository;
import com.wordcrush.server.module.user.avatar.service.AvatarStorageService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final GameRecordRepository gameRecordRepository;
    private final AvatarStorageService avatarStorageService;
    private final RankingCacheService rankingCacheService;

    @Transactional(readOnly = true)
    public List<RankingItemResponse> getTopRankings(RankingRequest request) {
        if (request == null) {
            throw new BusinessException("request body must not be null");
        }
        GameType.fromCode(request.gameType());
        if (request.topN() == null || request.topN() <= 0) {
            throw new BusinessException("topN must be greater than 0");
        }

        List<RankingCacheItem> ranking = rankingCacheService.getRanking(request.gameType());
        if (ranking == null) {
            ranking = loadRankingFromDatabase(request.gameType());
            rankingCacheService.cacheRanking(request.gameType(), ranking);
        }

        int limit = Math.min(request.topN(), ranking.size());
        return ranking.subList(0, limit).stream()
                .map(item -> new RankingItemResponse(
                        item.username(),
                        item.score(),
                        item.time(),
                        avatarStorageService.avatarVersion(item.username())
                ))
                .toList();
    }

    private List<RankingCacheItem> loadRankingFromDatabase(Integer gameType) {
        List<GameRecord> records = gameRecordRepository.findAllByGameTypeOrderByScoreDescPlayedAtAsc(gameType);
        LinkedHashMap<String, RankingCacheItem> rankingMap = new LinkedHashMap<>();
        for (GameRecord record : records) {
            String username = record.getUser().getUsername();
            if (rankingMap.containsKey(username)) {
                continue;
            }
            rankingMap.put(username, new RankingCacheItem(
                    username,
                    record.getScore(),
                    TimeFormats.formatGameTime(record.getPlayedAt())
            ));
        }
        return new ArrayList<>(rankingMap.values());
    }
}
