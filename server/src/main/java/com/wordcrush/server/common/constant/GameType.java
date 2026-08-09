package com.wordcrush.server.common.constant;

import com.wordcrush.server.common.exception.BusinessException;
import java.util.Arrays;

public enum GameType {
    CLASSIC(0),
    TIMED(1);

    private final int code;

    GameType(int code) {
        this.code = code;
    }

    public static GameType fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElseThrow(() -> new BusinessException(400, "unsupported gameType"));
    }
}
