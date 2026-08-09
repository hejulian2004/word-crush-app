package com.wordcrush.server.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(int code, String msg, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiCode.SUCCESS.value(), "success", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(ApiCode.SUCCESS.value(), "success", null);
    }

    public static ApiResponse<Void> fail(ApiCode code, String msg) {
        return new ApiResponse<>(code.value(), msg, null);
    }
}
