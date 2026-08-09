package com.wordcrush.server.module.game.record.dto;

import java.util.List;

public record SaveGameRecordRequest(
        String username,
        Integer gameType,
        Integer score,
        String time,
        List<String> learnedWords
) {
}
