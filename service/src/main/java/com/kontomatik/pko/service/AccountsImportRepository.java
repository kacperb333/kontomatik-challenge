package com.kontomatik.pko.service;

import java.util.List;
import java.util.Optional;

public interface AccountsImportRepository {
    AccountsImport store(AccountsImport accountsImport);

    Optional<AccountsImport> fetch(AccountsImportId accountsImportId);

    List<AccountsImport> fetchAll(OwnerId ownerId);
}
