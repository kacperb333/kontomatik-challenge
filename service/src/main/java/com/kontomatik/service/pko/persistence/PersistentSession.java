package com.kontomatik.service.pko.persistence;

import com.kontomatik.lib.pko.domain.accounts.AccountsInfo;
import com.kontomatik.lib.pko.domain.login.LoginInProgressPkoSession;
import com.kontomatik.service.pko.domain.ImportFailedSession;
import com.kontomatik.service.pko.domain.ImportFinishedSession;
import com.kontomatik.service.pko.domain.LoginInProgressSession;
import com.kontomatik.service.pko.domain.SessionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sessions")
@TypeAlias("PersistentSession")
class PersistentSession {
  @Id
  final SessionId sessionId;
  final LoginInProgressPkoSession pkoSession;
  final AccountsInfo accountsInfo;
  final boolean isFailed;
  final Instant persistedAt;

  @PersistenceCreator
  PersistentSession(
    SessionId sessionId,
    LoginInProgressPkoSession pkoSession,
    AccountsInfo accountsInfo,
    boolean isFailed,
    Instant persistedAt
  ) {
    this.sessionId = sessionId;
    this.pkoSession = pkoSession;
    this.accountsInfo = accountsInfo;
    this.isFailed = isFailed;
    this.persistedAt = persistedAt;
  }

  static PersistentSession fromDomain(LoginInProgressSession domainSession, Instant at) {
    return new PersistentSession(
      domainSession.sessionId(),
      domainSession.pkoSession(),
      AccountsInfo.EMPTY,
      false,
      at
    );
  }

  static PersistentSession fromDomain(ImportFinishedSession domainSession, Instant at) {
    return new PersistentSession(
      domainSession.sessionId(),
      null,
      domainSession.accountsInfo(),
      false,
      at
    );
  }

  static PersistentSession fromDomain(ImportFailedSession domainSession, Instant at) {
    return new PersistentSession(
      domainSession.sessionId(),
      null,
      AccountsInfo.EMPTY,
      true,
      at
    );
  }
}
