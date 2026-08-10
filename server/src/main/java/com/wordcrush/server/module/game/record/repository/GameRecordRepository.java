package com.wordcrush.server.module.game.record.repository;

import com.wordcrush.server.module.game.record.entity.GameRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    List<GameRecord> findAllByGameTypeOrderByScoreDescPlayedAtAsc(Integer gameType);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"learnedWords"})
    List<GameRecord> findByUserIdOrderByPlayedAtDesc(Long userId);

    Optional<GameRecord> findFirstByUserIdAndGameTypeAndScoreAndPlayedAt(
            Long userId,
            Integer gameType,
            Integer score,
            LocalDateTime playedAt);

    boolean existsByUserIdAndGameTypeAndScoreAndPlayedAt(
            Long userId,
            Integer gameType,
            Integer score,
            LocalDateTime playedAt);
}
