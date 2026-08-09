package com.wordcrush.server.module.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.wordcrush.server.module.user.account.dto.LoginRequest;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import com.wordcrush.server.module.user.account.response.UserResponse;
import com.wordcrush.server.security.TokenService;
import com.wordcrush.server.security.TokenSession;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRevokeExistingTokensBeforeIssuingNewLoginToken() {
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("encoded-password");

        TokenSession session = new TokenSession(
                1L,
                "admin",
                "fresh-token",
                LocalDateTime.of(2026, 4, 3, 10, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0)
        );

        when(userAccountRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(tokenService.issueToken(user)).thenReturn(session);

        UserResponse response = userService.login(new LoginRequest("admin", "123456"));

        assertThat(response.token()).isEqualTo("fresh-token");
        InOrder inOrder = inOrder(tokenService);
        inOrder.verify(tokenService).revokeUserTokens(1L);
        inOrder.verify(tokenService).issueToken(user);
    }
}
