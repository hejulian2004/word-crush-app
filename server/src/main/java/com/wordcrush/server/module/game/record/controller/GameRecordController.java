package com.wordcrush.server.module.game.record.controller;

import com.wordcrush.server.common.api.ApiResponse;
import com.wordcrush.server.module.game.ranking.dto.RankingRequest;
import com.wordcrush.server.module.game.ranking.response.RankingItemResponse;
import com.wordcrush.server.module.game.ranking.service.RankingService;
import com.wordcrush.server.module.game.record.dto.DeleteGameRecordRequest;
import com.wordcrush.server.module.game.record.dto.SaveGameRecordRequest;
import com.wordcrush.server.module.game.record.dto.UsernameRequest;
import com.wordcrush.server.module.game.record.response.GameRecordResponse;
import com.wordcrush.server.module.game.record.service.GameRecordService;
import com.wordcrush.server.security.AuthenticatedUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "GameRecord", description = "鎺掕姒滀笌娓告垙璁板綍鎺ュ彛")
public class GameRecordController {

    private final GameRecordService gameRecordService;
    private final RankingService rankingService;

    @PostMapping("/getTopNRecord")
    @Operation(summary = "鑾峰彇鎺掕姒?")
    public ApiResponse<List<RankingItemResponse>> getTopNRecord(@RequestBody RankingRequest request) {
        return ApiResponse.success(rankingService.getTopRankings(request));
    }

    @PostMapping("/addGameRecord")
    @Operation(summary = "鏂板娓告垙璁板綍")
    public ApiResponse<Void> addGameRecord(@RequestBody SaveGameRecordRequest request) {
        if (request != null) {
            AuthenticatedUserContext.requireAccessToUsername(request.username());
        }
        gameRecordService.addGameRecord(request);
        return ApiResponse.success();
    }

    @PostMapping("/deleteGameRecord")
    @Operation(summary = "鍒犻櫎娓告垙璁板綍")
    public ApiResponse<Void> deleteGameRecord(@RequestBody DeleteGameRecordRequest request) {
        if (request != null) {
            AuthenticatedUserContext.requireAccessToUsername(request.username());
        }
        gameRecordService.deleteGameRecord(request);
        return ApiResponse.success();
    }

    @PostMapping("/getAllGameRecord")
    @Operation(summary = "鑾峰彇鐢ㄦ埛鍏ㄩ儴娓告垙璁板綍")
    public ApiResponse<List<GameRecordResponse>> getAllGameRecord(@RequestBody UsernameRequest request) {
        if (request != null) {
            AuthenticatedUserContext.requireAccessToUsername(request.username());
        }
        return ApiResponse.success(gameRecordService.getAllGameRecords(request));
    }
}
