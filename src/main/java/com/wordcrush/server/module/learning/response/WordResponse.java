package com.wordcrush.server.module.learning.response;

public record WordResponse(
        Integer id,
        String english,
        String pronunciation,
        String chinese,
        Long contentVersion,
        Integer masterCount,
        Boolean mastered
) {
}
