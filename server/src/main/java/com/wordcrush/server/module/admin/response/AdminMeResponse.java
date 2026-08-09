package com.wordcrush.server.module.admin.response;

public record AdminMeResponse(
        Long id,
        String username,
        String role
) {
}
