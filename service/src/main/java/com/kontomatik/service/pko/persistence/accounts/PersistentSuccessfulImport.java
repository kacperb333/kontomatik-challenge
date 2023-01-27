package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;
import com.kontomatik.service.pko.domain.SessionId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

import java.time.Instant;

@TypeAlias("PersistentSuccessfulImport")
class PersistentSuccessfulImport extends PersistentImport {
  final Accounts accounts;

  @PersistenceCreator
  PersistentSuccessfulImport(
    SessionId sessionId,
    Accounts accounts,
    Instant persistedAt
  ) {
    super(
      sessionId,
      persistedAt
    );
    this.accounts = accounts;
  }

  static PersistentSuccessfulImport fromDomain(SuccessfulImport domainImport, Instant at) {
    return new PersistentSuccessfulImport(
      domainImport.sessionId(),
      domainImport.accounts(),
      at
    );
  }

  @Override
  SuccessfulImport toDomain() {
    return new SuccessfulImport(sessionId, accounts);
  }
}
