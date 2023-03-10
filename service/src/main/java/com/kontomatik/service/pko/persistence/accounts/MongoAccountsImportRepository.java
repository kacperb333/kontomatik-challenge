package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.service.common.DateTimeProvider;
import com.kontomatik.service.pko.domain.AccountsImportRepository;
import com.kontomatik.service.pko.domain.FinishedImport;
import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;
import com.kontomatik.service.pko.domain.FinishedImport.ImportId;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;
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
  public SuccessfulImport save(SuccessfulImport successfulImport) {
    var persistentSession = PersistentSuccessfulImport.fromDomain(successfulImport, dateTimeProvider.now());
    var saved = accountsImportRepository.save(persistentSession);
    return new SuccessfulImport(saved.importId, saved.accounts);
  }

  @Override
  public FailedImport save(FailedImport failedImport) {
    var persistentSession = PersistentFailedImport.fromDomain(failedImport, dateTimeProvider.now());
    var saved = accountsImportRepository.save(persistentSession);
    return new FailedImport(saved.importId);
  }

  @Override
  public Optional<FinishedImport> findAccountsImport(ImportId importId) {
    return accountsImportRepository.findById(importId)
      .map(PersistentImport::toDomain);
  }
}
