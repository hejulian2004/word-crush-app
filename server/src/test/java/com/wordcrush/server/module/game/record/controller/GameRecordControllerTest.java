package com.wordcrush.server.module.game.record.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordcrush.api.ApiCode;
import com.wordcrush.server.config.SecurityConfig;
import com.wordcrush.server.module.game.ranking.dto.RankingRequest;
import com.wordcrush.server.module.game.ranking.response.RankingItemResponse;
import com.wordcrush.server.module.game.ranking.service.RankingService;
import com.wordcrush.server.module.game.record.service.GameRecordService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameRecordController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class GameRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameRecordService gameRecordService;

    @MockBean
    private RankingService rankingService;

    @Test
    void shouldReturnStandardRankingResponse() throws Exception {
        when(rankingService.getTopRankings(any(RankingRequest.class))).thenReturn(List.of(
                new RankingItemResponse("alice", 25, "2026-04-02-10:11:12.345", 0L)
        ));

        mockMvc.perform(post("/api/getTopNRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RankingRequest(0, 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiCode.SUCCESS.value()))
                .andExpect(jsonPath("$.data[0].username").value("alice"))
                .andExpect(jsonPath("$.data[0].score").value(25));
    }
}
