package com.kontomatik.pko.service.domain.accounts;

import com.kontomatik.pko.service.domain.session.SessionId;

public record ScheduledAccountsImport(
  SessionId sessionId,
  AccountsImportId accountsImportId
) {
}
