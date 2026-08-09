package com.wordcrush.server.module.game.record.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.server.config.SecurityConfig;
import com.wordcrush.server.module.game.ranking.service.RankingService;
import com.wordcrush.server.module.game.record.dto.SaveGameRecordRequest;
import com.wordcrush.server.module.game.record.dto.UsernameRequest;
import com.wordcrush.server.module.game.record.service.GameRecordService;
import com.wordcrush.server.security.TokenService;
import com.wordcrush.server.security.TokenSession;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameRecordController.class)
@Import(SecurityConfig.class)
class GameRecordControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameRecordService gameRecordService;

    @MockBean
    private RankingService rankingService;

    @MockBean
    private TokenService tokenService;

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        SaveGameRecordRequest request = new SaveGameRecordRequest(
                "alice",
                0,
                25,
                "2026-04-03-10:11:12.345",
                List.of("apple")
        );

        mockMvc.perform(post("/api/addGameRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("token must not be blank"));
    }

    @Test
    void shouldRejectLegacyTokenHeader() throws Exception {
        mockMvc.perform(post("/api/getAllGameRecord")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsernameRequest("alice"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldRejectOperatingOnAnotherUsersRecords() throws Exception {
        when(tokenService.requireValidSession("valid-token")).thenReturn(session("alice", "valid-token"));

        mockMvc.perform(post("/api/getAllGameRecord")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsernameRequest("bob"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.msg").value("cannot operate on another user's data"));
    }

    @Test
    void shouldAllowAuthenticatedUserToReadOwnRecords() throws Exception {
        when(tokenService.requireValidSession("valid-token")).thenReturn(session("alice", "valid-token"));
        when(gameRecordService.getAllGameRecords(any(UsernameRequest.class))).thenReturn(List.of());

        mockMvc.perform(post("/api/getAllGameRecord")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsernameRequest("alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(gameRecordService).getAllGameRecords(new UsernameRequest("alice"));
    }

    private TokenSession session(String username, String token) {
        return new TokenSession(
                1L,
                username,
                token,
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0)
        );
    }
}
