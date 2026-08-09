package com.wordcrush.server.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int code, String msg, T data) {

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }

    public static ApiResponse<Void> success(String msg) {
        return new ApiResponse<>(200, msg, null);
    }

    public static ApiResponse<Void> fail(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}
