package com.kontomatik.pko;

import java.time.Instant;

public record AccountsImport(
    AccountsImportId accountsImportId,
    OwnerId ownerId,
    Status status,
    Instant createdAt,
    AccountsInfo accountsInfo
) {
    static AccountsImport inProgress(
        AccountsImportId accountsImportId,
        OwnerId ownerId,
        Instant createdAt
    ) {
        return new AccountsImport(
            accountsImportId,
            ownerId,
            Status.IN_PROGRESS,
            createdAt,
            AccountsInfo.EMPTY
        );
    }

    static AccountsImport success(
        AccountsImportId accountsImportId,
        OwnerId ownerId,
        AccountsInfo accountsInfo,
        Instant createdAt
    ) {
        return new AccountsImport(
            accountsImportId,
            ownerId,
            Status.SUCCESS,
            createdAt,
            accountsInfo
        );
    }

    static AccountsImport failure(
        AccountsImportId accountsImportId,
        OwnerId ownerId,
        Instant createdAt
    ) {
        return new AccountsImport(
            accountsImportId,
            ownerId,
            Status.FAILED,
            createdAt,
            AccountsInfo.EMPTY
        );
    }

    enum Status {
        IN_PROGRESS, SUCCESS, FAILED
    }
}
