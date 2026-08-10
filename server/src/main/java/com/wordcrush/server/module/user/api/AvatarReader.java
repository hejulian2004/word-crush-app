package com.wordcrush.server.module.user.api;

/**
 * Public avatar read capability used by the game ranking module.
 */
public interface AvatarReader {

    long avatarVersion(String username);
}
