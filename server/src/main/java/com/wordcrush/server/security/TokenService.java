package com.wordcrush.server.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.config.JwtProperties;
import com.wordcrush.server.module.user.account.entity.UserAccount;
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
public class TokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Value("${app.token.prefix}")
    private String tokenPrefix;

    @Value("${app.token.user-prefix}")
    private String userTokenPrefix;

    public TokenSession issueToken(UserAccount user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusHours(jwtProperties.getExpirationHours());
        TokenSession session = new TokenSession(user.getId(), user.getUsername(), token, issuedAt, expiresAt);
        persistSession(session, Duration.ofHours(jwtProperties.getExpirationHours()));
        return session;
    }

    public TokenSession requireValidSession(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, "token must not be blank");
        }
        Claims claims = jwtTokenProvider.parseToken(token);
        String raw = stringRedisTemplate.opsForValue().get(tokenKey(token));
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException(401, "token expired");
        }
        try {
            TokenSession session = objectMapper.readValue(raw, TokenSession.class);
            if (!String.valueOf(session.userId()).equals(claims.getSubject())
                    || !session.username().equals(claims.get("username", String.class))) {
                throw new BusinessException(401, "invalid token");
            }
            return session;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "token session deserialize failed");
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
            throw new BusinessException(500, "token session serialize failed");
        }
    }

    private String tokenKey(String token) {
        return tokenPrefix + token;
    }

    private String userTokenKey(Long userId) {
        return userTokenPrefix + userId;
    }
}
