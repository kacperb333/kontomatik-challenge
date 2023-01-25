package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.service.pko.domain.FailedImport;
import com.kontomatik.service.pko.domain.SessionId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

import java.time.Instant;

@TypeAlias("PersistentFailedImport")
class PersistentFailedImport extends PersistentImport {

  @PersistenceCreator
  public PersistentFailedImport(
    SessionId sessionId,
    Instant persistedAt
  ) {
    super(
      sessionId,
      persistedAt
    );
  }

  static PersistentFailedImport fromDomain(FailedImport domainImport, Instant at) {
    return new PersistentFailedImport(
      domainImport.sessionId(),
      at
    );
  }

  @Override
  FailedImport toDomain() {
    return new FailedImport(sessionId);
  }
}
