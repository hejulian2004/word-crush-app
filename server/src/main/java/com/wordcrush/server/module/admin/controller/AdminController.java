package com.wordcrush.server.module.admin.controller;

import com.wordcrush.server.common.api.ApiResponse;
import com.wordcrush.server.module.admin.dto.CreateWordRequest;
import com.wordcrush.server.module.admin.dto.ResetPasswordRequest;
import com.wordcrush.server.module.admin.dto.UpdateUserStatusRequest;
import com.wordcrush.server.module.admin.dto.UpdateWordRequest;
import com.wordcrush.server.module.admin.response.AdminMeResponse;
import com.wordcrush.server.module.admin.response.AdminOverviewResponse;
import com.wordcrush.server.module.admin.response.AdminPageResponse;
import com.wordcrush.server.module.admin.response.AdminUserResponse;
import com.wordcrush.server.module.admin.response.AdminWordResponse;
import com.wordcrush.server.module.admin.response.WordImportResponse;
import com.wordcrush.server.module.admin.service.AdminService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> me() {
        return ApiResponse.success(adminService.me());
    }

    @GetMapping("/overview")
    public ApiResponse<AdminOverviewResponse> overview() {
        return ApiResponse.success(adminService.overview());
    }

    @GetMapping("/users")
    public ApiResponse<AdminPageResponse<AdminUserResponse>> users(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ApiResponse.success(adminService.listUsers(query, status, page, size));
    }

    @PutMapping("/users/{userId}/status")
    public ApiResponse<AdminUserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success(adminService.updateUserStatus(userId, request));
    }

    @PutMapping("/users/{userId}/password")
    public ApiResponse<Void> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        adminService.resetPassword(userId, request);
        return ApiResponse.success();
    }

    @GetMapping("/words")
    public ApiResponse<AdminPageResponse<AdminWordResponse>> words(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ApiResponse.success(adminService.listWords(query, status, page, size));
    }

    @PostMapping("/words")
    public ApiResponse<AdminWordResponse> createWord(@Valid @RequestBody CreateWordRequest request) {
        return ApiResponse.success(adminService.createWord(request));
    }

    @PutMapping("/words/{wordId}")
    public ApiResponse<AdminWordResponse> updateWord(
            @PathVariable Integer wordId,
            @Valid @RequestBody UpdateWordRequest request
    ) {
        return ApiResponse.success(adminService.updateWord(wordId, request));
    }

    @PostMapping(value = "/words/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<WordImportResponse> importWords(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean replace
    ) {
        return ApiResponse.success(adminService.importWords(file, replace));
    }

    @GetMapping("/words/export")
    public ResponseEntity<byte[]> exportWords() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename("wordbook.csv", StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(adminService.exportWords());
    }
}
