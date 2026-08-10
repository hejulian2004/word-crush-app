package com.wordcrush.server.module.learning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wordcrush.server.module.learning.api.LearningWordAdminFacade;
import com.wordcrush.server.module.learning.entity.LearningWord;
import com.wordcrush.server.module.learning.repository.LearningWordRepository;
import com.wordcrush.server.module.user.api.UserAdminFacade;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LearningWordAdminServiceTest {

    @Mock
    private LearningWordRepository learningWordRepository;

    @Mock
    private UserAdminFacade userAdminFacade;

    @InjectMocks
    private LearningWordAdminService learningWordAdminService;

    @Test
    void shouldCreateWordThroughLearningModuleFacade() {
        when(userAdminFacade.currentAdmin(1L)).thenReturn(
                new UserAdminFacade.AdminUserView(1L, "admin", "ADMIN", 1, null, null));
        when(learningWordRepository.existsById(10)).thenReturn(false);
        when(learningWordRepository.save(any(LearningWord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LearningWordAdminFacade.WordView view = learningWordAdminService.createWord(
                1L, 10, "apple", "/ˈæpəl/", "苹果");

        assertThat(view.id()).isEqualTo(10);
        assertThat(view.english()).isEqualTo("apple");
        assertThat(view.status()).isEqualTo(1);
        assertThat(view.contentVersion()).isEqualTo(1L);
    }
}
