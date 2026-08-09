package com.wordcrush.server.module.user.avatar.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.avatar.cache-prefix}")
    private String cachePrefix;

    @Value("${app.avatar.cache-ttl}")
    private Duration cacheTtl;

    public Long getAvatarVersion(String username) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey(username));
            if (cached == null) {
                return null;
            }
            return Long.parseLong(cached);
        } catch (NumberFormatException exception) {
            log.warn("Failed to parse avatar cache for username={}", username, exception);
            evictAvatarVersion(username);
            return null;
        } catch (RuntimeException exception) {
            log.warn("Failed to read avatar cache for username={}", username, exception);
            return null;
        }
    }

    public void cacheAvatarVersion(String username, long avatarVersion) {
        try {
            stringRedisTemplate.opsForValue().set(cacheKey(username), String.valueOf(avatarVersion), cacheTtl);
        } catch (RuntimeException exception) {
            log.warn("Failed to write avatar cache for username={}", username, exception);
        }
    }

    public void evictAvatarVersion(String username) {
        try {
            stringRedisTemplate.delete(cacheKey(username));
        } catch (RuntimeException exception) {
            log.warn("Failed to evict avatar cache for username={}", username, exception);
        }
    }

    private String cacheKey(String username) {
        return cachePrefix + username;
    }
}
