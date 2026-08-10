package com.wordcrush.server.module.user.api;

import java.util.Collection;
import java.util.Map;

/**
 * Public read-only user capability for other business modules.
 */
public interface UserDirectory {

    UserSnapshot requireUser(Long userId);

    UserSnapshot requireUserByUsername(String username);

    Map<Long, UserSnapshot> findByIds(Collection<Long> userIds);
}
