package com.wordcrush.server.module.user.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Administrative user use cases exposed to the admin module.
 */
public interface UserAdminFacade {

    AdminUserView currentAdmin(Long operatorId);

    AdminSummary summary(Long operatorId);

    AdminUserPage listUsers(Long operatorId, String query, Integer status, Integer page, Integer size);

    AdminUserView updateUserStatus(Long operatorId, Long userId, Integer status);

    void resetPassword(Long operatorId, Long userId, String password);

    record AdminUserView(
            Long id,
            String username,
            String role,
            Integer status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    record AdminUserPage(
            List<AdminUserView> items,
            int page,
            int size,
            long total
    ) {
    }

    record AdminSummary(
            long totalUsers,
            long activeUsers
    ) {
    }
}
