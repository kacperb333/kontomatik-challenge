package com.kontomatik.pko.service.domain.accounts;

import java.time.Instant;
import java.util.Optional;

public interface AccountsImportRepository {
  AccountsImport store(AccountsImport accountsImport);

  Optional<AccountsImport> fetchOneNewerThan(AccountsImportId accountsImportId, Instant maxTime);
}
