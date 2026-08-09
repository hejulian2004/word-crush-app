package com.wordcrush.server.module.game.ranking.service;

public record RankingCacheItem(
        String username,
        Integer score,
        String time
) {
}
