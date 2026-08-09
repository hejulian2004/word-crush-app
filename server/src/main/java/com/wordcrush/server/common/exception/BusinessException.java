package com.wordcrush.server.common.exception;

import com.wordcrush.server.common.api.ApiCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ApiCode code;

    public BusinessException(ApiCode code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(ApiCode.BAD_REQUEST, message);
    }
}
