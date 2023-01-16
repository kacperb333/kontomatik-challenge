package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.FinishedSession;
import com.kontomatik.pko.service.domain.ImportFailedSession;
import com.kontomatik.pko.service.domain.SessionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "finished_session")
@TypeAlias("PersistentImportFailedSession")
class PersistentImportFailedSession implements PersistentFinishedSession {
  @Id
  private final String sessionId;
  private final Instant createdAt;

  @PersistenceCreator
  PersistentImportFailedSession(String sessionId, Instant createdAt) {
    this.sessionId = sessionId;
    this.createdAt = createdAt;
  }

  static PersistentImportFailedSession fromDomain(ImportFailedSession domainSession) {
    return new PersistentImportFailedSession(
      domainSession.sessionId().value(),
      domainSession.createdAt()
    );
  }

  @Override
  public FinishedSession toDomain() {
    return new ImportFailedSession(
      new SessionId(sessionId),
      createdAt
    );
  }
}
