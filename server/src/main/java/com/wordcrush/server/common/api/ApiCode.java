package com.wordcrush.server.common.api;

import org.springframework.http.HttpStatus;

/**
 * HTTP-aligned response codes used by the JSON API.
 */
public enum ApiCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    CONFLICT(409),
    UNSUPPORTED_MEDIA_TYPE(415),
    INTERNAL_SERVER_ERROR(500);

    private final int value;

    ApiCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public HttpStatus httpStatus() {
        return HttpStatus.valueOf(value);
    }
}
