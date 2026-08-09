package com.wordcrush.server.module.game.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wordcrush.server.module.game.ranking.dto.RankingRequest;
import com.wordcrush.server.module.game.ranking.response.RankingItemResponse;
import com.wordcrush.server.module.game.record.entity.GameRecord;
import com.wordcrush.server.module.game.record.repository.GameRecordRepository;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.avatar.service.AvatarStorageService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Mock
    private AvatarStorageService avatarStorageService;

    @Mock
    private RankingCacheService rankingCacheService;

    @InjectMocks
    private RankingService rankingService;

    @Test
    void shouldReturnCachedRankingSubset() {
        List<RankingCacheItem> cached = List.of(
                new RankingCacheItem("alice", 30, "2026-04-03-10:00:00.000"),
                new RankingCacheItem("bob", 20, "2026-04-03-10:01:00.000")
        );
        when(rankingCacheService.getRanking(0)).thenReturn(cached);
        when(avatarStorageService.avatarVersion("alice")).thenReturn(11L);

        List<RankingItemResponse> result = rankingService.getTopRankings(new RankingRequest(0, 1));

        assertThat(result).containsExactly(new RankingItemResponse("alice", 30, "2026-04-03-10:00:00.000", 11L));
        verify(rankingCacheService).getRanking(0);
    }

    @Test
    void shouldLoadAndCacheRankingWhenCacheMiss() {
        GameRecord aliceBest = record("alice", 30, LocalDateTime.of(2026, 4, 3, 10, 0));
        GameRecord aliceOlder = record("alice", 20, LocalDateTime.of(2026, 4, 3, 11, 0));
        GameRecord bob = record("bob", 18, LocalDateTime.of(2026, 4, 3, 12, 0));
        when(rankingCacheService.getRanking(0)).thenReturn(null);
        when(gameRecordRepository.findAllByGameTypeOrderByScoreDescPlayedAtAsc(0))
                .thenReturn(List.of(aliceBest, aliceOlder, bob));
        when(avatarStorageService.avatarVersion("alice")).thenReturn(111L);
        when(avatarStorageService.avatarVersion("bob")).thenReturn(222L);

        List<RankingItemResponse> result = rankingService.getTopRankings(new RankingRequest(0, 2));

        assertThat(result).containsExactly(
                new RankingItemResponse("alice", 30, "2026-04-03-10:00:00.000", 111L),
                new RankingItemResponse("bob", 18, "2026-04-03-12:00:00.000", 222L)
        );
        ArgumentCaptor<List<RankingCacheItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(rankingCacheService).cacheRanking(eq(0), captor.capture());
        assertThat(captor.getValue()).containsExactly(
                new RankingCacheItem("alice", 30, "2026-04-03-10:00:00.000"),
                new RankingCacheItem("bob", 18, "2026-04-03-12:00:00.000")
        );
    }

    private GameRecord record(String username, int score, LocalDateTime playedAt) {
        UserAccount user = new UserAccount();
        user.setUsername(username);

        GameRecord record = new GameRecord();
        record.setUser(user);
        record.setScore(score);
        record.setPlayedAt(playedAt);
        return record;
    }
}
