package com.kontomatik.service.pko.persistence.session;

import com.kontomatik.lib.pko.domain.signin.OtpRequiredPkoSession;
import com.kontomatik.service.pko.domain.OtpRequiredSession;
import com.kontomatik.service.pko.domain.SessionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sessions")
@TypeAlias("PersistentSession")
class PersistentSession {
  @Id
  final SessionId sessionId;
  final OtpRequiredPkoSession pkoSession;
  @Indexed(expireAfter = "5m")
  final Instant persistedAt;

  @PersistenceCreator
  PersistentSession(
    SessionId sessionId,
    OtpRequiredPkoSession pkoSession,
    Instant persistedAt
  ) {
    this.sessionId = sessionId;
    this.pkoSession = pkoSession;
    this.persistedAt = persistedAt;
  }

  static PersistentSession fromDomain(OtpRequiredSession domainSession, Instant at) {
    return new PersistentSession(
      domainSession.sessionId(),
      domainSession.pkoSession(),
      at
    );
  }
}
