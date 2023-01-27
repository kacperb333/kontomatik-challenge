package com.kontomatik.service.pko.domain;

import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;

import java.util.Optional;

public interface AccountsImportRepository {
  SuccessfulImport save(SuccessfulImport successfulImport);

  FailedImport save(FailedImport failedImport);

  Optional<FinishedImport> findAccountsImport(SessionId sessionId);
}
