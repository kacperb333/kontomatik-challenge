package com.kontomatik.pko;

public record ScheduledAccountsImport(
    OwnerSessionId ownerSessionId,
    AccountsImportId accountsImportId
) {
}
