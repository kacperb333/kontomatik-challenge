package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.service.pko.domain.FinishedImport;
import com.kontomatik.service.pko.domain.SessionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "imports")
abstract class PersistentImport {
  @Id
  final SessionId sessionId;
  @Indexed(expireAfter = "24h")
  final Instant persistedAt;

  PersistentImport(SessionId sessionId, Instant persistedAt) {
    this.sessionId = sessionId;
    this.persistedAt = persistedAt;
  }

  abstract FinishedImport toDomain();
}
