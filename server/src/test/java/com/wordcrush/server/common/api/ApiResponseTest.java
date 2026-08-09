package com.wordcrush.server.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.common.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void successResponseAlwaysContainsExplicitNullData() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(ApiResponse.success()));

        assertThat(json.get("code").asInt()).isEqualTo(ApiCode.SUCCESS.value());
        assertThat(json.get("msg").asText()).isEqualTo("success");
        assertThat(json.has("data")).isTrue();
        assertThat(json.get("data").isNull()).isTrue();
    }

    @Test
    void businessResponsesUseTheSameHttpAndEnvelopeCode() {
        List<ApiCode> codes = List.of(
                ApiCode.BAD_REQUEST,
                ApiCode.UNAUTHORIZED,
                ApiCode.FORBIDDEN,
                ApiCode.NOT_FOUND,
                ApiCode.CONFLICT,
                ApiCode.INTERNAL_SERVER_ERROR
        );

        for (ApiCode code : codes) {
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler
                    .handleBusinessException(new BusinessException(code, "details"));

            assertThat(response.getStatusCode().value()).isEqualTo(code.value());
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo(code.value());
            assertThat(response.getBody().msg()).isEqualTo("details");
            assertThat(response.getBody().data()).isNull();
        }
    }
}
