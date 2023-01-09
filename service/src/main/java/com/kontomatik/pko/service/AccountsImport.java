package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.AccountsInfo;

public record AccountsImport(
    AccountsImportId accountsImportId,
    OwnerId ownerId,
    AccountsInfo accountsInfo
) {
}
