package com.wordcrush.server.module.user.avatar.response;

public record AvatarUploadResponse(
        String username,
        String avatarUrl,
        Long avatarVersion
) {
}
