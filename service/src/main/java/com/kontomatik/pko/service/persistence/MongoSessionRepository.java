package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.*;
import org.springframework.stereotype.Repository;

@Repository
class MongoSessionRepository implements SessionRepository {

  private final PersistentLoginInProgressSessionRepository loginInProgressSessionRepository;
  private final PersistentFinishedSessionRepository finishedSessionRepository;

  MongoSessionRepository(
    PersistentLoginInProgressSessionRepository loginInProgressSessionRepository,
    PersistentFinishedSessionRepository finishedSessionRepository
  ) {
    this.loginInProgressSessionRepository = loginInProgressSessionRepository;
    this.finishedSessionRepository = finishedSessionRepository;
  }

  @Override
  public LoginInProgressSession save(LoginInProgressSession loginInProgressSession) {
    return loginInProgressSessionRepository.save(PersistentLoginInProgressSession.fromDomain(loginInProgressSession))
      .toDomain();
  }

  @Override
  public FinishedSession save(FinishedSession finishedSession) {
    var persistentSession = switch (finishedSession) {
      case ImportFinishedSession s -> PersistentImportFinishedSession.fromDomain(s);
      case ImportFailedSession s -> PersistentImportFailedSession.fromDomain(s);
    };
    return finishedSessionRepository.save(persistentSession).toDomain();
  }

  @Override
  public LoginInProgressSession getLoginInProgressSession(SessionId sessionId) {
    return loginInProgressSessionRepository.findById(sessionId.value())
      .map(PersistentLoginInProgressSession::toDomain)
      .orElseThrow(() -> new SessionNotFound(sessionId));
  }

  @Override
  public FinishedSession getFinishedSession(SessionId sessionId) {
    return finishedSessionRepository.findById(sessionId.value())
      .map(PersistentFinishedSession::toDomain)
      .orElseThrow(() -> new SessionNotFound(sessionId));
  }
}
