package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemorySessionRepository implements SessionRepository {
  private final ConcurrentMap<SessionId, LoginInProgressSession> inProgressSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<SessionId, FinishedSession> finishedSessions = new ConcurrentHashMap<>();

  @Override
  public LoginInProgressSession save(LoginInProgressSession loginInProgressSession) {
    var key = loginInProgressSession.sessionId();
    inProgressSessions.put(key, loginInProgressSession);
    return loginInProgressSession;
  }

  @Override
  public FinishedSession save(FinishedSession finishedSession) {
    var key = finishedSession.sessionId();
    finishedSessions.put(key, finishedSession);
    return finishedSession;
  }

  @Override
  public LoginInProgressSession getLoginInProgressSession(SessionId sessionId) {
    LoginInProgressSession inProgressSession = inProgressSessions.get(sessionId);
    if (inProgressSession != null) {
      return inProgressSession;
    } else {
      throw new SessionNotFound(sessionId);
    }
  }

  @Override
  public FinishedSession getFinishedSession(SessionId sessionId) {
    FinishedSession finishedSession = finishedSessions.get(sessionId);
    if (finishedSession != null) {
      return finishedSession;
    } else {
      throw new SessionNotFound(sessionId);
    }
  }
}
