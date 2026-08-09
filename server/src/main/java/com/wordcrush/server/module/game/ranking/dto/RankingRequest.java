package com.wordcrush.server.module.game.ranking.dto;

public record RankingRequest(
        Integer gameType,
        Integer topN
) {
}
