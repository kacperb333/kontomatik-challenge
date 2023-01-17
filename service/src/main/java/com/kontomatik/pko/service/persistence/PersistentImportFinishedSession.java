package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.service.domain.FinishedSession;
import com.kontomatik.pko.service.domain.ImportFinishedSession;
import com.kontomatik.pko.service.domain.SessionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sessions")
@TypeAlias("PersistentImportFinishedSession")
class PersistentImportFinishedSession implements PersistentFinishedSession {
  @Id
  private final String sessionId;
  private final AccountsInfo accountsInfo;
  private final Instant persistedAt;

  @PersistenceCreator
  PersistentImportFinishedSession(String sessionId, AccountsInfo accountsInfo, Instant persistedAt) {
    this.sessionId = sessionId;
    this.accountsInfo = accountsInfo;
    this.persistedAt = persistedAt;
  }

  static PersistentImportFinishedSession fromDomain(ImportFinishedSession domainSession, Instant at) {
    return new PersistentImportFinishedSession(
      domainSession.sessionId().value(),
      domainSession.accountsInfo(),
      at
    );
  }

  @Override
  public FinishedSession toDomain() {
    return new ImportFinishedSession(
      new SessionId(sessionId),
      accountsInfo
    );
  }
}
