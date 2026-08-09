package com.wordcrush.server.module.user.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username must not be blank")
        @Size(min = 3, max = 32, message = "username length must be between 3 and 32")
        String username,

        @NotBlank(message = "password must not be blank")
        @Size(min = 6, max = 64, message = "password length must be between 6 and 64")
        String password
) {
}
