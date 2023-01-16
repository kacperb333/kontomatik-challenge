package com.kontomatik.pko.service.domain.accounts;

import java.time.Instant;
import java.util.Optional;

public interface AccountsImportRepository {
  AccountsImport save(AccountsImport accountsImport);

  Optional<AccountsImport> findOneNewerThan(AccountsImportId accountsImportId, Instant maxTime);
}
