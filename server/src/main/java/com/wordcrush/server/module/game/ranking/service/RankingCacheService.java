package com.wordcrush.server.module.game.ranking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ranking.prefix}")
    private String rankingPrefix;

    @Value("${app.ranking.ttl}")
    private Duration rankingTtl;

    public List<RankingCacheItem> getRanking(Integer gameType) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(rankingKey(gameType));
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, rankingListType());
        } catch (JsonProcessingException exception) {
            log.warn("Failed to deserialize ranking cache for gameType={}", gameType, exception);
            evictGameType(gameType);
            return null;
        } catch (RuntimeException exception) {
            log.warn("Failed to read ranking cache for gameType={}", gameType, exception);
            return null;
        }
    }

    public void cacheRanking(Integer gameType, List<RankingCacheItem> ranking) {
        try {
            String payload = objectMapper.writeValueAsString(ranking);
            stringRedisTemplate.opsForValue().set(rankingKey(gameType), payload, rankingTtl);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize ranking cache for gameType={}", gameType, exception);
        } catch (RuntimeException exception) {
            log.warn("Failed to write ranking cache for gameType={}", gameType, exception);
        }
    }

    public void evictGameType(Integer gameType) {
        try {
            stringRedisTemplate.delete(rankingKey(gameType));
        } catch (RuntimeException exception) {
            log.warn("Failed to evict ranking cache for gameType={}", gameType, exception);
        }
    }

    private String rankingKey(Integer gameType) {
        return rankingPrefix + gameType;
    }

    private JavaType rankingListType() {
        return objectMapper.getTypeFactory().constructCollectionType(List.class, RankingCacheItem.class);
    }
}
