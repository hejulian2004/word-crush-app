package com.wordcrush.server.module.game.record.dto;

public record DeleteGameRecordRequest(
        String username,
        Integer gameType,
        Integer score,
        String time
) {
}
