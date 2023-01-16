package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.lib.usecase.login.LoginInProgressPkoSession;
import com.kontomatik.pko.service.domain.LoginInProgressSession;
import com.kontomatik.pko.service.domain.SessionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "login_in_progress_session")
@TypeAlias("PersistentLoginInProgressSession")
class PersistentLoginInProgressSession {
  @Id
  private final String sessionId;
  private final LoginInProgressPkoSession pkoSession;

  @PersistenceCreator
  PersistentLoginInProgressSession(String sessionId, LoginInProgressPkoSession pkoSession) {
    this.sessionId = sessionId;
    this.pkoSession = pkoSession;
  }

  static PersistentLoginInProgressSession fromDomain(LoginInProgressSession domainSession) {
    return new PersistentLoginInProgressSession(
      domainSession.sessionId().value(),
      domainSession.pkoSession()
    );
  }

  LoginInProgressSession toDomain() {
    return new LoginInProgressSession(
      new SessionId(sessionId),
      pkoSession
    );
  }
}
