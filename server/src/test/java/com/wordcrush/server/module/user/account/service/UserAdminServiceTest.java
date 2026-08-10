package com.wordcrush.server.module.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import com.wordcrush.server.module.user.api.UserAdminFacade;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void shouldExposeOnlySafeAdminUserData() {
        UserAccount admin = new UserAccount();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPasswordHash("secret-hash");
        admin.setRole(UserAccount.ROLE_ADMIN);
        admin.setStatus(1);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserAdminFacade.AdminUserView view = userAdminService.currentAdmin(1L);

        assertThat(view.id()).isEqualTo(1L);
        assertThat(view.username()).isEqualTo("admin");
        assertThat(view.role()).isEqualTo(UserAccount.ROLE_ADMIN);
    }
}
