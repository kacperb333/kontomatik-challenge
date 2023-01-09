package com.kontomatik.pko.service;

public record ScheduledAccountsImport(
    OwnerSessionId ownerSessionId,
    AccountsImportId accountsImportId
) {
}
