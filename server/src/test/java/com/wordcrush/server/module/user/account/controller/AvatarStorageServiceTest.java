package com.wordcrush.server.module.user.avatar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AvatarStorageServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private AvatarCacheService avatarCacheService;

    @TempDir
    Path tempDir;

    @Test
    void shouldRefreshAvatarCacheAfterUploadingAvatar() throws Exception {
        AvatarStorageService avatarStorageService =
                new AvatarStorageService(userAccountRepository, avatarCacheService, tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar".getBytes()
        );
        when(userAccountRepository.existsByUsername("alice")).thenReturn(true);

        String avatarUrl = avatarStorageService.storeAvatar("alice", file);

        assertThat(avatarUrl).isEqualTo("/api/user/avatar/alice");
        try (var storedFiles = Files.list(tempDir)) {
            assertThat(storedFiles.toList()).hasSize(1);
        }
        verify(avatarCacheService).cacheAvatarVersion(org.mockito.ArgumentMatchers.eq("alice"), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void shouldReturnAvatarVersionFromCacheWhenPresent() {
        AvatarStorageService avatarStorageService =
                new AvatarStorageService(userAccountRepository, avatarCacheService, tempDir.toString());
        when(avatarCacheService.getAvatarVersion("alice")).thenReturn(123L);

        long avatarVersion = avatarStorageService.avatarVersion("alice");

        assertThat(avatarVersion).isEqualTo(123L);
    }
}
