package com.wordcrush.server.module.admin.response;

import java.util.List;

public record AdminPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long total
) {
}
