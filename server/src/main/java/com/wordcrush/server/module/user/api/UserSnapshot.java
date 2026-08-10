package com.wordcrush.server.module.user.api;

/**
 * Read-only user information that can safely cross a module boundary.
 * Password hashes and persistence details are intentionally excluded.
 */
public record UserSnapshot(
        Long id,
        String username,
        String role,
        Integer status
) {

    public boolean isActive() {
        return Integer.valueOf(1).equals(status);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
