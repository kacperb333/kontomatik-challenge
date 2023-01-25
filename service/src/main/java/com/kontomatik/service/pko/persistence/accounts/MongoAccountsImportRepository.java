package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.service.common.DateTimeProvider;
import com.kontomatik.service.pko.domain.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class MongoAccountsImportRepository implements AccountsImportRepository {
  private final SpringPersistentAccountsImportRepository accountsImportRepository;
  private final DateTimeProvider dateTimeProvider;

  MongoAccountsImportRepository(
    SpringPersistentAccountsImportRepository accountsImportRepository,
    DateTimeProvider dateTimeProvider
  ) {
    this.accountsImportRepository = accountsImportRepository;
    this.dateTimeProvider = dateTimeProvider;
  }

  @Override
  public SuccessfulImport save(SuccessfulImport finishedSession) {
    var persistentSession = PersistentSuccessfulImport.fromDomain(finishedSession, dateTimeProvider.now());
    var saved = accountsImportRepository.save(persistentSession);
    return new SuccessfulImport(saved.sessionId, saved.accounts);
  }

  @Override
  public FailedImport save(FailedImport importFailedSession) {
    var persistentSession = PersistentFailedImport.fromDomain(importFailedSession, dateTimeProvider.now());
    var saved = accountsImportRepository.save(persistentSession);
    return new FailedImport(saved.sessionId);
  }

  @Override
  public Optional<FinishedImport> findAccountsImport(SessionId sessionId) {
    return accountsImportRepository.findById(sessionId)
      .map(PersistentImport::toDomain);
  }
}
