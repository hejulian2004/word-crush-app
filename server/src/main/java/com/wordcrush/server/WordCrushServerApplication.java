package com.wordcrush.server;

import com.wordcrush.server.config.BootstrapAdminProperties;
import com.wordcrush.server.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        BootstrapAdminProperties.class
})
public class WordCrushServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WordCrushServerApplication.class, args);
    }
}
