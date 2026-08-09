package com.wordcrush.server.module.admin.response;

public record WordImportResponse(
        int added,
        int updated,
        int disabled,
        int total,
        int skipped
) {
}
