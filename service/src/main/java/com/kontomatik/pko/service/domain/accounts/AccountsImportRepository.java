package com.kontomatik.pko.service.domain.accounts;

import com.kontomatik.pko.service.domain.session.OwnerId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccountsImportRepository {
  AccountsImport store(AccountsImport accountsImport);

  Optional<AccountsImport> fetchOneNewerThan(AccountsImportId accountsImportId, Instant maxTime);

  List<AccountsImport> fetchAllForOwnerNewerThan(OwnerId ownerId, Instant maxTime);
}
