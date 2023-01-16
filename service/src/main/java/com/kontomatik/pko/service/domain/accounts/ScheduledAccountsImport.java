package com.kontomatik.pko.service.domain.accounts;

import com.kontomatik.pko.service.domain.session.OwnerSessionId;

public record ScheduledAccountsImport(
  OwnerSessionId ownerSessionId,
  AccountsImportId accountsImportId
) {
}
