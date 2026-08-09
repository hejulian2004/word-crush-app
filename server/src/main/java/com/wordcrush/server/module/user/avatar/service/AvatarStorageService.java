package com.wordcrush.server.module.user.avatar.service;

import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AvatarStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private final UserAccountRepository userAccountRepository;
    private final AvatarCacheService avatarCacheService;
    private final Path storageDirectory;

    public AvatarStorageService(
            UserAccountRepository userAccountRepository,
            AvatarCacheService avatarCacheService,
            @Value("${app.avatar.storage-path:storage/avatars}") String storagePath) {
        this.userAccountRepository = userAccountRepository;
        this.avatarCacheService = avatarCacheService;
        this.storageDirectory = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public String storeAvatar(String username, MultipartFile file) {
        validateUser(username);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "avatar file must not be empty");
        }

        String extension = resolveExtension(file);
        String encodedUsername = encodeUsername(username);

        try {
            Files.createDirectories(storageDirectory);
            deleteExistingAvatars(encodedUsername);

            Path target = storageDirectory.resolve(encodedUsername + "." + extension);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Files.setLastModifiedTime(target, FileTime.from(Instant.now()));
            avatarCacheService.cacheAvatarVersion(username, lastModified(target).toMillis());
            return avatarUrl(username);
        } catch (IOException exception) {
            throw new BusinessException(500, "avatar upload failed");
        }
    }

    public Resource loadAvatar(String username) {
        validateUser(username);
        Path avatarPath = findAvatarPath(username);
        if (avatarPath == null) {
            throw new BusinessException(404, "avatar not found");
        }

        try {
            Resource resource = new UrlResource(avatarPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(404, "avatar not found");
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw new BusinessException(500, "avatar read failed");
        }
    }

    public String detectContentType(String username) {
        Path avatarPath = findAvatarPath(username);
        if (avatarPath == null) {
            throw new BusinessException(404, "avatar not found");
        }

        try {
            String detected = Files.probeContentType(avatarPath);
            return StringUtils.hasText(detected) ? detected : "application/octet-stream";
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    public String avatarUrl(String username) {
        return "/api/user/avatar/" + username;
    }

    public long avatarVersion(String username) {
        Long cached = avatarCacheService.getAvatarVersion(username);
        if (cached != null) {
            return cached;
        }
        Path avatarPath = findAvatarPath(username);
        long avatarVersion = avatarPath == null ? 0L : lastModified(avatarPath).toMillis();
        avatarCacheService.cacheAvatarVersion(username, avatarVersion);
        return avatarVersion;
    }

    private void validateUser(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(400, "username must not be blank");
        }
        if (!userAccountRepository.existsByUsername(username)) {
            throw new BusinessException(404, "user not found");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException(400, "unsupported avatar file type");
        }

        String normalized = extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(normalized)) {
            throw new BusinessException(400, "unsupported avatar file type");
        }
        return normalized;
    }

    private void deleteExistingAvatars(String encodedUsername) throws IOException {
        if (!Files.isDirectory(storageDirectory)) {
            return;
        }

        try (var stream = Files.list(storageDirectory)) {
            stream.filter(path -> path.getFileName().toString().startsWith(encodedUsername + "."))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
    }

    private Path findAvatarPath(String username) {
        if (!Files.isDirectory(storageDirectory)) {
            return null;
        }

        String encodedUsername = encodeUsername(username);
        try (var stream = Files.list(storageDirectory)) {
            return stream.filter(path -> path.getFileName().toString().startsWith(encodedUsername + "."))
                    .max(Comparator.comparing(this::lastModified))
                    .orElse(null);
        } catch (IOException exception) {
            throw new BusinessException(500, "avatar read failed");
        }
    }

    private FileTime lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException exception) {
            return FileTime.fromMillis(0);
        }
    }

    private String encodeUsername(String username) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(username.getBytes(StandardCharsets.UTF_8));
    }
}
