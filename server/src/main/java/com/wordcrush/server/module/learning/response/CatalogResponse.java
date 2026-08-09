package com.wordcrush.server.module.learning.response;

import java.util.List;

public record CatalogResponse(
        List<WordResponse> items,
        int page,
        int size,
        long total,
        long catalogVersion
) {
}
