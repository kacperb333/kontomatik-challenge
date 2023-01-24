package com.kontomatik.service.pko.persistence;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.signin.OtpRequiredPkoSession;
import com.kontomatik.service.pko.domain.ImportFailedSession;
import com.kontomatik.service.pko.domain.ImportFinishedSession;
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
  final Accounts accounts;
  final boolean isFailed;
  @Indexed(expireAfter = "24h")
  final Instant persistedAt;

  @PersistenceCreator
  PersistentSession(
    SessionId sessionId,
    OtpRequiredPkoSession pkoSession,
    Accounts accounts,
    boolean isFailed,
    Instant persistedAt
  ) {
    this.sessionId = sessionId;
    this.pkoSession = pkoSession;
    this.accounts = accounts;
    this.isFailed = isFailed;
    this.persistedAt = persistedAt;
  }

  static PersistentSession fromDomain(OtpRequiredSession domainSession, Instant at) {
    return new PersistentSession(
      domainSession.sessionId(),
      domainSession.pkoSession(),
      Accounts.EMPTY,
      false,
      at
    );
  }

  static PersistentSession fromDomain(ImportFinishedSession domainSession, Instant at) {
    return new PersistentSession(
      domainSession.sessionId(),
      null,
      domainSession.accounts(),
      false,
      at
    );
  }

  static PersistentSession fromDomain(ImportFailedSession domainSession, Instant at) {
    return new PersistentSession(
      domainSession.sessionId(),
      null,
      Accounts.EMPTY,
      true,
      at
    );
  }
}
