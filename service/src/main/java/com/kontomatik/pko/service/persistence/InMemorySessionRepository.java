package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.session.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemorySessionRepository implements SessionRepository {

  private final ConcurrentMap<SessionId, InitialSession> initialSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<SessionId, LoginInProgressSession> inProgressSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<SessionId, LoggedInSession> loggedInSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<SessionId, FinishedSession> finishedSessions = new ConcurrentHashMap<>();

  @Override
  public InitialSession store(InitialSession initialSession) {
    initialSessions.put(initialSession.SessionId(), initialSession);
    return initialSession;
  }

  @Override
  public LoginInProgressSession store(LoginInProgressSession loginInProgressSession) {
    var key = loginInProgressSession.sessionId();
    initialSessions.remove(key);
    inProgressSessions.put(key, loginInProgressSession);
    return loginInProgressSession;
  }

  @Override
  public LoggedInSession store(LoggedInSession loggedInSession) {
    var key = loggedInSession.sessionId();
    inProgressSessions.remove(key);
    loggedInSessions.put(key, loggedInSession);
    return loggedInSession;
  }

  @Override
  public FinishedSession store(FinishedSession finishedSession) {
    var key = finishedSession.sessionId();
    loggedInSessions.remove(key);
    finishedSessions.put(key, finishedSession);
    return finishedSession;
  }

  @Override
  public Optional<InitialSession> fetchInitialSession(SessionId sessionId) {
    return Optional.ofNullable(initialSessions.get(sessionId));
  }

  @Override
  public Optional<LoginInProgressSession> fetchLoginInProgressSession(SessionId sessionId) {
    return Optional.ofNullable(inProgressSessions.get(sessionId));
  }

  @Override
  public Optional<LoggedInSession> fetchLoggedInSession(SessionId sessionId) {
    return Optional.ofNullable(loggedInSessions.get(sessionId));
  }
}
