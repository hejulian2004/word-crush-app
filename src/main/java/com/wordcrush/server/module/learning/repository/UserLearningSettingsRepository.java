package com.wordcrush.server.module.learning.repository;

import com.wordcrush.server.module.learning.entity.UserLearningSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLearningSettingsRepository extends JpaRepository<UserLearningSettings, Long> {

    Optional<UserLearningSettings> findByUser_Id(Long userId);
}
