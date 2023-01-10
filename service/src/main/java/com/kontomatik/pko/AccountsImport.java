package com.kontomatik.pko;

import java.time.Instant;

public record AccountsImport(
    AccountsImportId accountsImportId,
    OwnerId ownerId,
    AccountsInfo accountsInfo,
    Status status,
    Instant createdAt
) {
    static AccountsImport inProgress(
        AccountsImportId accountsImportId,
        OwnerId ownerId,
        Instant createdAt
    ) {
        return new AccountsImport(
            accountsImportId,
            ownerId,
            AccountsInfo.EMPTY,
            Status.IN_PROGRESS,
            createdAt
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
            accountsInfo,
            Status.SUCCESS,
            createdAt
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
            AccountsInfo.EMPTY,
            Status.FAILED,
            createdAt
        );
    }

    enum Status {
        IN_PROGRESS, SUCCESS, FAILED
    }
}
