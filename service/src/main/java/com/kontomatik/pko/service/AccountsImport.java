package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.AccountsInfo;

import java.time.Instant;

public record AccountsImport(
    AccountsImportId accountsImportId,
    OwnerId ownerId,
    AccountsInfo accountsInfo,
    Instant createdAt
) {
}
