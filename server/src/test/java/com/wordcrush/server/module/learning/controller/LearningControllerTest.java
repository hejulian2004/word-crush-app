package com.wordcrush.server.module.learning.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.api.ApiCode;
import com.wordcrush.server.config.SecurityConfig;
import com.wordcrush.server.module.learning.dto.DailyTargetRequest;
import com.wordcrush.server.module.learning.dto.LearningSyncRequest;
import com.wordcrush.server.module.learning.response.CatalogResponse;
import com.wordcrush.server.module.learning.response.LearningStateResponse;
import com.wordcrush.server.module.learning.response.LearningSyncResponse;
import com.wordcrush.server.module.learning.response.WordResponse;
import com.wordcrush.server.module.learning.service.LearningService;
import com.wordcrush.server.security.api.TokenSession;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LearningController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class LearningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LearningService learningService;

    @BeforeEach
    void setSecurityContext() {
        TokenSession session = new TokenSession(
                1L,
                "alice",
                "token",
                LocalDateTime.of(2026, 8, 9, 10, 0),
                LocalDateTime.of(2026, 8, 16, 10, 0)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(session, session.token(), List.of())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCatalogWithProgressFields() throws Exception {
        when(learningService.getCatalog(any(), any(), any(), any(), any(), any()))
                .thenReturn(new CatalogResponse(
                        List.of(new WordResponse(1, "abandon", "əˈbændən", "放弃", 1L, 2, false)),
                        0,
                        100,
                        1,
                        1L
                ));

        mockMvc.perform(get("/api/learning/catalog")
                        .param("query", "abandon")
                        .param("mastered", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiCode.SUCCESS.value()))
                .andExpect(jsonPath("$.data.items[0].english").value("abandon"))
                .andExpect(jsonPath("$.data.items[0].masterCount").value(2));
    }

    @Test
    void shouldUpdateDailyTarget() throws Exception {
        when(learningService.updateDailyTarget(any(), any(DailyTargetRequest.class)))
                .thenReturn(state());

        mockMvc.perform(put("/api/learning/settings/daily-target")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DailyTargetRequest(20))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dailyTarget").value(20));
    }

    @Test
    void shouldSyncMutations() throws Exception {
        when(learningService.sync(any(), any(LearningSyncRequest.class)))
                .thenReturn(new LearningSyncResponse(List.of("mutation-1"), state()));

        mockMvc.perform(post("/api/learning/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LearningSyncRequest(List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acceptedMutationIds[0]").value("mutation-1"));
    }

    private LearningStateResponse state() {
        return new LearningStateResponse(
                20,
                "2026-08-09",
                List.of(1),
                List.of(new WordResponse(1, "abandon", "əˈbændən", "放弃", 1L, 0, false)),
                0,
                1,
                false,
                false,
                false,
                0L,
                List.of()
        );
    }
}
