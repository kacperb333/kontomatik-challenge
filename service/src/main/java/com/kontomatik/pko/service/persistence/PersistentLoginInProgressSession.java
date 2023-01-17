package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.lib.usecase.login.LoginInProgressPkoSession;
import com.kontomatik.pko.service.domain.LoginInProgressSession;
import com.kontomatik.pko.service.domain.SessionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sessions")
@TypeAlias("PersistentLoginInProgressSession")
class PersistentLoginInProgressSession {
  @Id
  private final String sessionId;
  private final LoginInProgressPkoSession pkoSession;
  private final Instant persistedAt;

  @PersistenceCreator
  PersistentLoginInProgressSession(String sessionId, LoginInProgressPkoSession pkoSession, Instant persistedAt) {
    this.sessionId = sessionId;
    this.pkoSession = pkoSession;
    this.persistedAt = persistedAt;
  }

  static PersistentLoginInProgressSession fromDomain(LoginInProgressSession domainSession, Instant at) {
    return new PersistentLoginInProgressSession(
      domainSession.sessionId().value(),
      domainSession.pkoSession(),
      at
    );
  }

  LoginInProgressSession toDomain() {
    return new LoginInProgressSession(
      new SessionId(sessionId),
      pkoSession
    );
  }
}
