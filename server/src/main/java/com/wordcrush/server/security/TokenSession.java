package com.wordcrush.server.security;

import java.time.LocalDateTime;

public record TokenSession(
        Long userId,
        String username,
        String token,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt
) {
}
