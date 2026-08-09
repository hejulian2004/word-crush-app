package com.wordcrush.server.module.game.record.repository;

import com.wordcrush.server.module.game.record.entity.GameRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<GameRecord> findAllByGameTypeOrderByScoreDescPlayedAtAsc(Integer gameType);

    @EntityGraph(attributePaths = {"user", "learnedWords"})
    List<GameRecord> findByUserUsernameOrderByPlayedAtDesc(String username);

    @EntityGraph(attributePaths = {"user", "learnedWords"})
    Optional<GameRecord> findFirstByUserUsernameAndGameTypeAndScoreAndPlayedAt(
            String username,
            Integer gameType,
            Integer score,
            LocalDateTime playedAt);

    boolean existsByUserUsernameAndGameTypeAndScoreAndPlayedAt(
            String username,
            Integer gameType,
            Integer score,
            LocalDateTime playedAt);
}
