package com.wordcrush.server.module.learning.repository;

import com.wordcrush.server.module.learning.entity.UserWordProgress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    @EntityGraph(attributePaths = {"word"})
    List<UserWordProgress> findByUser_Id(Long userId);

    @EntityGraph(attributePaths = {"word"})
    List<UserWordProgress> findByUser_IdAndWord_IdIn(Long userId, Collection<Integer> wordIds);

    @EntityGraph(attributePaths = {"word"})
    Optional<UserWordProgress> findByUser_IdAndWord_Id(Long userId, Integer wordId);
}
