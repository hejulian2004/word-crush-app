package com.wordcrush.server.security.api;

public interface TokenIssuer {

    TokenSession issueToken(Long userId, String username);

    void revokeUserTokens(Long userId);
}
