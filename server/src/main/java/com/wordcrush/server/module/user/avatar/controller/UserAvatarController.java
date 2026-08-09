package com.wordcrush.server.module.user.avatar.controller;

import com.wordcrush.server.common.api.ApiResponse;
import com.wordcrush.server.module.user.avatar.response.AvatarUploadResponse;
import com.wordcrush.server.module.user.avatar.service.AvatarStorageService;
import com.wordcrush.server.security.AuthenticatedUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "Avatar", description = "鐢ㄦ埛澶村儚鎺ュ彛")
public class UserAvatarController {

    private final AvatarStorageService avatarStorageService;

    @PostMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "涓婁紶澶村儚")
    public ApiResponse<AvatarUploadResponse> uploadAvatar(
            @RequestParam @NotBlank(message = "username must not be blank") String username,
            @RequestParam("file") MultipartFile file) {
        AuthenticatedUserContext.requireAccessToUsername(username);
        String avatarUrl = avatarStorageService.storeAvatar(username, file);
        return ApiResponse.success("avatar uploaded", new AvatarUploadResponse(
                username,
                avatarUrl,
                avatarStorageService.avatarVersion(username)
        ));
    }

    @GetMapping("/avatar/{username}")
    @Operation(summary = "鑾峰彇澶村儚")
    public ResponseEntity<Resource> getAvatar(@PathVariable String username) {
        Resource resource = avatarStorageService.loadAvatar(username);
        MediaType mediaType = MediaType.parseMediaType(avatarStorageService.detectContentType(username));
        long avatarVersion = avatarStorageService.avatarVersion(username);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .lastModified(avatarVersion)
                .contentType(mediaType)
                .body(resource);
    }
}
