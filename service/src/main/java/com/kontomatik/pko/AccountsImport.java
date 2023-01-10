package com.kontomatik.pko;

import java.time.Instant;

public record AccountsImport(
    AccountsImportId accountsImportId,
    OwnerId ownerId,
    AccountsInfo accountsInfo,
    Instant createdAt
) {
}
