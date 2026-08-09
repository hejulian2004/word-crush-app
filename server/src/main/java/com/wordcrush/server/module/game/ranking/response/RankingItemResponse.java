package com.wordcrush.server.module.game.ranking.response;

public record RankingItemResponse(
        String username,
        Integer score,
        String time,
        Long avatarVersion
) {
}
