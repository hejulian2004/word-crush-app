package com.wordcrush.server.module.user.account.service;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.exception.BusinessException;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import com.wordcrush.server.module.user.account.repository.UserAccountRepository;
import com.wordcrush.server.module.user.api.UserAdminFacade;
import com.wordcrush.server.module.user.api.UserSnapshot;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService implements UserAdminFacade {

    private static final int ACTIVE_STATUS = 1;

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AdminUserView currentAdmin(Long operatorId) {
        return toView(requireAdmin(operatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSummary summary(Long operatorId) {
        requireAdmin(operatorId);
        List<UserAccount> users = userAccountRepository.findAll();
        return new AdminSummary(
                users.size(),
                users.stream().filter(this::isActive).count()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserPage listUsers(Long operatorId, String query, Integer status, Integer page, Integer size) {
        requireAdmin(operatorId);
        String normalizedQuery = normalize(query);
        List<AdminUserView> filtered = userAccountRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(user -> normalizedQuery.isBlank()
                        || user.getUsername().toLowerCase().contains(normalizedQuery))
                .filter(user -> status == null || status.equals(user.getStatus()))
                .map(this::toView)
                .toList();
        return page(filtered, page, size);
    }

    @Override
    @Transactional
    public AdminUserView updateUserStatus(Long operatorId, Long userId, Integer status) {
        UserAccount admin = requireAdmin(operatorId);
        UserAccount user = findUser(userId);
        int nextStatus = normalizeStatus(status);
        if (admin.getId().equals(user.getId()) && nextStatus == 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "cannot disable the current admin account");
        }
        if (nextStatus == 0 && UserAccount.ROLE_ADMIN.equals(user.getRole())
                && activeAdminCount() <= 1) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "at least one active admin is required");
        }
        user.setStatus(nextStatus);
        return toView(userAccountRepository.save(user));
    }

    @Override
    @Transactional
    public void resetPassword(Long operatorId, Long userId, String password) {
        requireAdmin(operatorId);
        UserAccount user = findUser(userId);
        user.setPasswordHash(passwordEncoder.encode(password));
        userAccountRepository.save(user);
    }

    private UserAccount requireAdmin(Long operatorId) {
        UserAccount user = userAccountRepository.findById(operatorId)
                .orElseThrow(() -> new BusinessException(ApiCode.UNAUTHORIZED, "invalid token"));
        if (!isActive(user) || !UserAccount.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ApiCode.FORBIDDEN, "admin access required");
        }
        return user;
    }

    private UserAccount findUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ApiCode.NOT_FOUND, "user not found"));
    }

    private long activeAdminCount() {
        return userAccountRepository.findAll().stream()
                .filter(user -> UserAccount.ROLE_ADMIN.equals(user.getRole()) && isActive(user))
                .count();
    }

    private AdminUserView toView(UserAccount user) {
        return new AdminUserView(
                user.getId(), user.getUsername(), user.getRole(), user.getStatus(),
                user.getCreatedAt(), user.getUpdatedAt());
    }

    private AdminUserPage page(List<AdminUserView> items, Integer page, Integer size) {
        int normalizedPage = Math.max(page == null ? 0 : page, 0);
        int normalizedSize = Math.min(Math.max(size == null ? 20 : size, 1), 100);
        int from = Math.min(normalizedPage * normalizedSize, items.size());
        int to = Math.min(from + normalizedSize, items.size());
        return new AdminUserPage(items.subList(from, to), normalizedPage, normalizedSize, items.size());
    }

    private int normalizeStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "status must be 0 or 1");
        }
        return status;
    }

    private boolean isActive(UserAccount user) {
        return Integer.valueOf(ACTIVE_STATUS).equals(user.getStatus());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
