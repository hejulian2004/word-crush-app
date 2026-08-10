package com.wordcrush.server.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.config.JwtProperties;
import com.wordcrush.server.security.api.TokenIssuer;
import com.wordcrush.server.security.api.TokenSession;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TokenService implements TokenIssuer {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Value("${app.token.prefix}")
    private String tokenPrefix;

    @Value("${app.token.user-prefix}")
    private String userTokenPrefix;

    @Override
    public TokenSession issueToken(Long userId, String username) {
        String token = jwtTokenProvider.generateToken(userId, username);
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusHours(jwtProperties.getExpirationHours());
        TokenSession session = new TokenSession(userId, username, token, issuedAt, expiresAt);
        persistSession(session, Duration.ofHours(jwtProperties.getExpirationHours()));
        return session;
    }

    public TokenSession requireValidSession(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "token must not be blank");
        }
        Claims claims = jwtTokenProvider.parseToken(token);
        String raw = stringRedisTemplate.opsForValue().get(tokenKey(token));
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "token expired");
        }
        try {
            TokenSession session = objectMapper.readValue(raw, TokenSession.class);
            if (!String.valueOf(session.userId()).equals(claims.getSubject())
                    || !session.username().equals(claims.get("username", String.class))) {
                throw new BusinessException(ApiCode.UNAUTHORIZED, "invalid token");
            }
            return session;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ApiCode.INTERNAL_SERVER_ERROR, "internal server error");
        }
    }

    public void revokeUserTokens(Long userId) {
        String userKey = userTokenKey(userId);
        Set<String> tokens = stringRedisTemplate.opsForSet().members(userKey);
        if (tokens != null && !tokens.isEmpty()) {
            stringRedisTemplate.delete(tokens.stream().map(this::tokenKey).toList());
        }
        stringRedisTemplate.delete(userKey);
    }

    private void persistSession(TokenSession session, Duration ttl) {
        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(tokenKey(session.token()), sessionJson, ttl);
            stringRedisTemplate.opsForSet().add(userTokenKey(session.userId()), session.token());
            stringRedisTemplate.expire(userTokenKey(session.userId()), ttl);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ApiCode.INTERNAL_SERVER_ERROR, "internal server error");
        }
    }

    private String tokenKey(String token) {
        return tokenPrefix + token;
    }

    private String userTokenKey(Long userId) {
        return userTokenPrefix + userId;
    }
}
