package com.wordcrush.api;

/**
 * HTTP-aligned response codes shared by the Android client and backend.
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
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503);

    private final int value;

    ApiCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
