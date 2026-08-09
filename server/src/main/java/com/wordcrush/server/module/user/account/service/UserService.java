package com.wordcrush.server.module.user.account.service;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.module.user.account.dto.LoginRequest;
import com.wordcrush.server.module.user.account.dto.RegisterRequest;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import com.wordcrush.server.module.user.account.response.UserResponse;
import com.wordcrush.server.security.TokenService;
import com.wordcrush.server.security.TokenSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ApiCode.UNAUTHORIZED, "invalid username or password"));
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ApiCode.FORBIDDEN, "account is disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "invalid username or password");
        }
        tokenService.revokeUserTokens(user.getId());
        return toUserResponse(user, tokenService.issueToken(user));
    }

    @Transactional(readOnly = true)
    public UserResponse checkToken(TokenSession session) {
        UserAccount user = userAccountRepository.findById(session.userId())
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "user not found"));
        return toUserResponse(user, session);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userAccountRepository.existsByUsername(username)) {
            throw new BusinessException(ApiCode.CONFLICT, "username already exists");
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(1);
        user.setRole(UserAccount.ROLE_USER);
        userAccountRepository.save(user);
        return toUserResponse(user, tokenService.issueToken(user));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "user not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        tokenService.revokeUserTokens(user.getId());
    }

    private UserResponse toUserResponse(UserAccount user, TokenSession session) {
        return new UserResponse(user.getUsername(), String.valueOf(user.getId()), session.token());
    }
}
