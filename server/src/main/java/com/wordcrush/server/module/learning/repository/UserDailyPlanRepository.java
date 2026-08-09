package com.wordcrush.server.module.learning.repository;

import com.wordcrush.server.module.learning.entity.UserDailyPlan;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyPlanRepository extends JpaRepository<UserDailyPlan, Long> {

    @EntityGraph(attributePaths = {"items", "items.word"})
    Optional<UserDailyPlan> findByUser_IdAndPlanDate(Long userId, LocalDate planDate);
}
