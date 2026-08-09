package com.wordcrush.server.module.learning.repository;

import com.wordcrush.server.module.learning.entity.LearningSyncMutation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningSyncMutationRepository extends JpaRepository<LearningSyncMutation, Long> {

    Optional<LearningSyncMutation> findByUser_IdAndMutationId(Long userId, String mutationId);
}
