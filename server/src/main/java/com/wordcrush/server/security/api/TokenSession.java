package com.wordcrush.server.security.api;

import java.time.LocalDateTime;

/**
 * Authentication session contract shared by web adapters and business modules.
 */
public record TokenSession(
        Long userId,
        String username,
        String token,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt
) {
}
