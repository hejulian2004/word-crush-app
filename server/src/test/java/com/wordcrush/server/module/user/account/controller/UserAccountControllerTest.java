package com.wordcrush.server.module.user.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.server.common.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.config.SecurityConfig;
import com.wordcrush.server.module.user.account.dto.LoginRequest;
import com.wordcrush.server.module.user.account.dto.RegisterRequest;
import com.wordcrush.server.module.user.account.response.UserResponse;
import com.wordcrush.server.module.user.account.service.UserService;
import com.wordcrush.server.security.TokenSession;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@WebMvcTest(UserAccountController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAccountControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnStandardLoginResponse() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(new UserResponse("admin", "1", "token-string"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.uid").value("1"));
    }

    @Test
    void shouldReturnRegisterResponse() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse("tom", "2", "new-token"));

                mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("tom", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.token").value("new-token"));
    }

    @Test
    void shouldReturnUnifiedInternalErrorResponse() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(ApiCode.INTERNAL_SERVER_ERROR, "internal server error"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "123456"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("internal server error"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void shouldReturnCheckTokenResponse() throws Exception {
        TokenSession session = new TokenSession(
                1L,
                "admin",
                "token-string",
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(session, session.token(), List.of())
        );
        when(userService.checkToken(session))
                .thenReturn(new UserResponse("admin", "1", "token-string"));

        mockMvc.perform(get("/api/user/checkToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }
}
