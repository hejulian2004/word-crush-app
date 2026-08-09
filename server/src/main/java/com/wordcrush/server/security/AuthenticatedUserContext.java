package com.wordcrush.server.security;

import com.wordcrush.server.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public final class AuthenticatedUserContext {

    private AuthenticatedUserContext() {
    }

    public static TokenSession requireCurrentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "invalid token");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof TokenSession session)) {
            throw new BusinessException(401, "invalid token");
        }
        return session;
    }

    public static TokenSession requireAccessToUsername(String username) {
        TokenSession session = requireCurrentSession();
        if (StringUtils.hasText(username) && !session.username().equals(username)) {
            throw new BusinessException(403, "cannot operate on another user's data");
        }
        return session;
    }
}
