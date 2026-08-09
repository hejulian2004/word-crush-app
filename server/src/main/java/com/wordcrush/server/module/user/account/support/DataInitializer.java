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
        userAccountRepository.findByUsername(bootstrapAdminProperties.getUsername())
                .orElseGet(() -> {
                    UserAccount admin = new UserAccount();
                    admin.setUsername(bootstrapAdminProperties.getUsername());
                    admin.setPasswordHash(passwordEncoder.encode(bootstrapAdminProperties.getPassword()));
                    admin.setStatus(1);
                    log.info("Bootstrap admin account created: {}", admin.getUsername());
                    return userAccountRepository.save(admin);
                });
    }
}
