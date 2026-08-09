package com.wordcrush.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.server.security.TokenAuthenticationFilter;
import com.wordcrush.server.security.TokenService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<TokenService> tokenServiceProvider,
            ObjectMapper objectMapper
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/actuator/**").permitAll()
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        .requestMatchers("/api/getTopNRecord").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/avatar/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .cors(Customizer.withDefaults())
                .securityMatcher("/api/**", "/swagger-ui/**", "/api-docs/**", "/actuator/**");

        TokenService tokenService = tokenServiceProvider.getIfAvailable();
        if (tokenService != null) {
            http.addFilterBefore(
                    new TokenAuthenticationFilter(tokenService, objectMapper),
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
