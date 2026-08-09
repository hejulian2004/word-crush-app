package com.wordcrush.server.module.user.account.support;

import com.wordcrush.server.config.BootstrapAdminProperties;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    @Override
    public void run(ApplicationArguments args) {
        UserAccount admin = userAccountRepository.findByUsername(bootstrapAdminProperties.getUsername())
                .orElseGet(() -> {
                    UserAccount created = new UserAccount();
                    created.setUsername(bootstrapAdminProperties.getUsername());
                    created.setPasswordHash(passwordEncoder.encode(bootstrapAdminProperties.getPassword()));
                    created.setStatus(1);
                    created.setRole(UserAccount.ROLE_ADMIN);
                    log.info("Bootstrap admin account created: {}", created.getUsername());
                    return userAccountRepository.save(created);
                });
        if (!UserAccount.ROLE_ADMIN.equals(admin.getRole())) {
            admin.setRole(UserAccount.ROLE_ADMIN);
            userAccountRepository.save(admin);
            log.info("Bootstrap account promoted to admin: {}", admin.getUsername());
        }
    }
}
