package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.service.pko.domain.FinishedImport.ImportId;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

import java.time.Instant;

@TypeAlias("PersistentSuccessfulImport")
class PersistentSuccessfulImport extends PersistentImport {
  final Accounts accounts;

  @PersistenceCreator
  PersistentSuccessfulImport(
    ImportId importId,
    Accounts accounts,
    Instant persistedAt
  ) {
    super(
      importId,
      persistedAt
    );
    this.accounts = accounts;
  }

  static PersistentSuccessfulImport fromDomain(SuccessfulImport domainImport, Instant at) {
    return new PersistentSuccessfulImport(
      domainImport.importId(),
      domainImport.accounts(),
      at
    );
  }

  @Override
  SuccessfulImport toDomain() {
    return new SuccessfulImport(importId, accounts);
  }
}
