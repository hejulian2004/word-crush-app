package com.wordcrush.server.module.user.account.service;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import com.wordcrush.server.module.user.api.UserDirectory;
import com.wordcrush.server.module.user.api.UserSnapshot;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDirectoryService implements UserDirectory {

    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserSnapshot requireUser(Long userId) {
        return userAccountRepository.findById(userId)
                .map(this::toSnapshot)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "user not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserSnapshot requireUserByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .map(this::toSnapshot)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "user not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserSnapshot> findByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userAccountRepository.findAllById(userIds).stream()
                .map(this::toSnapshot)
                .collect(Collectors.toMap(UserSnapshot::id, Function.identity()));
    }

    private UserSnapshot toSnapshot(UserAccount user) {
        return new UserSnapshot(user.getId(), user.getUsername(), user.getRole(), user.getStatus());
    }
}
