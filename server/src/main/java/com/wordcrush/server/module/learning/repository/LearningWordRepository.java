package com.wordcrush.server.module.learning.repository;

import com.wordcrush.server.module.learning.entity.LearningWord;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningWordRepository extends JpaRepository<LearningWord, Integer> {

    List<LearningWord> findAllByStatusOrderByIdAsc(Integer status);

    List<LearningWord> findAllByIdInAndStatusOrderByIdAsc(Collection<Integer> ids, Integer status);

    Optional<LearningWord> findFirstByEnglish(String english);
}
