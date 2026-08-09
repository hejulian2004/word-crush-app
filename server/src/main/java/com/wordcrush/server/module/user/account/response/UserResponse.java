package com.wordcrush.server.module.user.account.response;

public record UserResponse(
        String username,
        String uid,
        String token
) {
}
