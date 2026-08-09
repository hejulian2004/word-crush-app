package com.wordcrush.server.module.game.record.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wordcrush.server.module.game.ranking.service.RankingCacheService;
import com.wordcrush.server.module.game.record.dto.DeleteGameRecordRequest;
import com.wordcrush.server.module.game.record.dto.SaveGameRecordRequest;
import com.wordcrush.server.module.game.record.entity.GameRecord;
import com.wordcrush.server.module.game.record.repository.GameRecordRepository;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameRecordServiceTest {

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private RankingCacheService rankingCacheService;

    @InjectMocks
    private GameRecordService gameRecordService;

    @Test
    void shouldEvictRankingCacheAfterAddingRecord() {
        UserAccount user = new UserAccount();
        user.setUsername("alice");
        SaveGameRecordRequest request = new SaveGameRecordRequest(
                "alice",
                0,
                25,
                "2026-04-03-10:11:12.345",
                List.of("apple", "banana")
        );
        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(gameRecordRepository.existsByUserUsernameAndGameTypeAndScoreAndPlayedAt(
                "alice",
                0,
                25,
                LocalDateTime.of(2026, 4, 3, 10, 11, 12, 345_000_000)))
                .thenReturn(false);

        gameRecordService.addGameRecord(request);

        verify(gameRecordRepository).save(org.mockito.ArgumentMatchers.any(GameRecord.class));
        verify(rankingCacheService).evictGameType(0);
    }

    @Test
    void shouldNotEvictRankingCacheWhenDuplicateRecordIsIgnored() {
        UserAccount user = new UserAccount();
        user.setUsername("alice");
        SaveGameRecordRequest request = new SaveGameRecordRequest(
                "alice",
                0,
                25,
                "2026-04-03-10:11:12.345",
                List.of()
        );
        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(gameRecordRepository.existsByUserUsernameAndGameTypeAndScoreAndPlayedAt(
                "alice",
                0,
                25,
                LocalDateTime.of(2026, 4, 3, 10, 11, 12, 345_000_000)))
                .thenReturn(true);

        gameRecordService.addGameRecord(request);

        verify(gameRecordRepository, never()).save(org.mockito.ArgumentMatchers.any(GameRecord.class));
        verify(rankingCacheService, never()).evictGameType(0);
    }

    @Test
    void shouldEvictRankingCacheAfterDeletingRecord() {
        GameRecord record = new GameRecord();
        DeleteGameRecordRequest request = new DeleteGameRecordRequest(
                "alice",
                1,
                30,
                "2026-04-03-10:11:12.345"
        );
        when(gameRecordRepository.findFirstByUserUsernameAndGameTypeAndScoreAndPlayedAt(
                "alice",
                1,
                30,
                LocalDateTime.of(2026, 4, 3, 10, 11, 12, 345_000_000)))
                .thenReturn(Optional.of(record));

        gameRecordService.deleteGameRecord(request);

        verify(gameRecordRepository).delete(record);
        verify(rankingCacheService).evictGameType(1);
    }
}
