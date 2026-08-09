package com.wordcrush.server.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    @NotBlank
    private String secret = "WordCrushJwtSecretKeyForResumeBuild1234567890";

    @Min(1)
    private long expirationHours = 168;
}
