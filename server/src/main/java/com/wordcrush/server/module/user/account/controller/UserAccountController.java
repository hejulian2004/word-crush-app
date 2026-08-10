package com.wordcrush.server.module.user.account.controller;

import com.wordcrush.server.common.api.ApiResponse;
import com.wordcrush.server.module.user.account.dto.LoginRequest;
import com.wordcrush.server.module.user.account.dto.RegisterRequest;
import com.wordcrush.server.module.user.account.response.UserResponse;
import com.wordcrush.server.module.user.account.service.UserService;
import com.wordcrush.server.security.api.AuthenticatedUserContext;
import com.wordcrush.server.security.api.TokenSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "User", description = "鐢ㄦ埛璐﹀彿鎺ュ彛")
public class UserAccountController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "鐧诲綍")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @GetMapping("/checkToken")
    @Operation(summary = "鏍￠獙 token")
    public ApiResponse<UserResponse> checkToken() {
        TokenSession session = AuthenticatedUserContext.requireCurrentSession();
        return ApiResponse.success(userService.checkToken(session));
    }

    @PostMapping("/register")
    @Operation(summary = "娉ㄥ唽")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/changePassword")
    @Operation(summary = "淇敼瀵嗙爜")
    public ApiResponse<Void> changePassword(
            @RequestParam @NotBlank(message = "username must not be blank") String username,
            @RequestParam @NotBlank(message = "oldPassword must not be blank") String oldPassword,
            @RequestParam @NotBlank(message = "newPassword must not be blank") String newPassword) {
        AuthenticatedUserContext.requireAccessToUsername(username);
        userService.changePassword(username, oldPassword, newPassword);
        return ApiResponse.success();
    }
}
