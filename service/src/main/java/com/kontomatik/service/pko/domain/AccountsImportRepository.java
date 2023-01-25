package com.kontomatik.service.pko.domain;

import java.util.Optional;

public interface AccountsImportRepository {
  SuccessfulImport save(SuccessfulImport finishedSession);

  FailedImport save(FailedImport importFailedSession);

  Optional<FinishedImport> findAccountsImport(SessionId sessionId);
}
