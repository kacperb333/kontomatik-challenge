package com.kontomatik.pko;

import java.time.Instant;

public record AccountsImport(
    AccountsImportId accountsImportId,
    OwnerId ownerId,
    Status status,
    Instant createdAt,
    AccountsInfo accountsInfo,
    Details details
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
            AccountsInfo.EMPTY,
            Details.EMPTY
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
            accountsInfo,
            Details.EMPTY
        );
    }

    static AccountsImport failure(
        AccountsImportId accountsImportId,
        OwnerId ownerId,
        Instant createdAt,
        Details details
    ) {
        return new AccountsImport(
            accountsImportId,
            ownerId,
            Status.FAILED,
            createdAt,
            AccountsInfo.EMPTY,
            details
        );
    }

    enum Status {
        IN_PROGRESS, SUCCESS, FAILED
    }

    record Details(
        String value
    ) {
        static final Details EMPTY = new Details(null);
        static Details ofMessage(String message) {
            return new Details(message);
        }
    }
}
