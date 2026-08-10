package com.wordcrush.server.module.learning.controller;

import com.wordcrush.server.common.api.ApiResponse;
import com.wordcrush.server.module.learning.dto.DailyTargetRequest;
import com.wordcrush.server.module.learning.dto.LearningSyncRequest;
import com.wordcrush.server.module.learning.response.CatalogResponse;
import com.wordcrush.server.module.learning.response.LearningStateResponse;
import com.wordcrush.server.module.learning.response.LearningSyncResponse;
import com.wordcrush.server.module.learning.service.LearningService;
import com.wordcrush.server.security.api.AuthenticatedUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/learning")
@Tag(name = "Learning", description = "单词学习、每日计划与进度同步接口")
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/catalog")
    @Operation(summary = "获取单词目录")
    public ApiResponse<CatalogResponse> getCatalog(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean mastered,
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        return ApiResponse.success(learningService.getCatalog(
                AuthenticatedUserContext.requireCurrentSession(), query, mastered, ids, page, size));
    }

    @GetMapping("/state")
    @Operation(summary = "获取学习状态")
    public ApiResponse<LearningStateResponse> getState() {
        return ApiResponse.success(learningService.getState(
                AuthenticatedUserContext.requireCurrentSession()));
    }

    @GetMapping("/plan")
    @Operation(summary = "获取每日学习计划")
    public ApiResponse<LearningStateResponse> getPlan() {
        return getState();
    }

    @PutMapping("/settings/daily-target")
    @Operation(summary = "更新每日学习数量")
    public ApiResponse<LearningStateResponse> updateDailyTarget(@Valid @RequestBody DailyTargetRequest request) {
        return ApiResponse.success(learningService.updateDailyTarget(
                AuthenticatedUserContext.requireCurrentSession(), request));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步离线学习进度")
    public ApiResponse<LearningSyncResponse> sync(@Valid @RequestBody LearningSyncRequest request) {
        return ApiResponse.success(learningService.sync(
                AuthenticatedUserContext.requireCurrentSession(), request));
    }
}
