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

@Document(collection = "finished_session")
@TypeAlias("PersistentImportFinishedSession")
class PersistentImportFinishedSession implements PersistentFinishedSession {
  @Id
  private final String sessionId;
  private final Instant createdAt;
  private final AccountsInfo accountsInfo;

  @PersistenceCreator
  PersistentImportFinishedSession(String sessionId, Instant createdAt, AccountsInfo accountsInfo) {
    this.sessionId = sessionId;
    this.createdAt = createdAt;
    this.accountsInfo = accountsInfo;
  }

  static PersistentImportFinishedSession fromDomain(ImportFinishedSession domainSession) {
    return new PersistentImportFinishedSession(
      domainSession.sessionId().value(),
      domainSession.createdAt(),
      domainSession.accountsInfo()
    );
  }

  @Override
  public FinishedSession toDomain() {
    return new ImportFinishedSession(
      new SessionId(sessionId),
      createdAt,
      accountsInfo
    );
  }
}
