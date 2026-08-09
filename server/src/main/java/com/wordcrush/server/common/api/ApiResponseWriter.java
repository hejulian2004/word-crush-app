package com.wordcrush.server.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.api.ApiCode;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class ApiResponseWriter {

    private ApiResponseWriter() {
    }

    public static ResponseEntity<ApiResponse<Void>> entity(ApiCode code, String message) {
        return ResponseEntity.status(code.value()).body(ApiResponse.fail(code, message));
    }

    public static void write(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            ApiCode code,
            String message
    ) throws IOException {
        response.setStatus(code.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(code, message));
    }
}
