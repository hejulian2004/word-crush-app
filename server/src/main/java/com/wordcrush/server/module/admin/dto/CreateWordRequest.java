package com.wordcrush.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWordRequest(
        @NotNull Integer id,
        @NotBlank @Size(max = 128) String english,
        @NotBlank @Size(max = 255) String pronunciation,
        @NotBlank @Size(max = 1024) String chinese
) {
}
