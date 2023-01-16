package com.kontomatik.pko.service.domain.accounts;

import java.util.Optional;

public interface AccountsImportRepository {
  AccountsImport save(AccountsImport accountsImport);

  Optional<AccountsImport> findOne(AccountsImportId accountsImportId);
}
