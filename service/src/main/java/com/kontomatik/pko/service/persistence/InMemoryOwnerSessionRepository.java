package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.session.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemoryOwnerSessionRepository implements SessionRepository {

  private final ConcurrentMap<OwnerSessionId, InitialSession> initialSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<OwnerSessionId, LoginInProgressSession> inProgressSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<OwnerSessionId, LoggedInSession> loggedInSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<OwnerSessionId, FinishedSession> finishedSessions = new ConcurrentHashMap<>();

  @Override
  public InitialSession store(InitialSession initialOwnerSession) {
    initialSessions.put(initialOwnerSession.ownerSessionId(), initialOwnerSession);
    return initialOwnerSession;
  }

  @Override
  public LoginInProgressSession store(LoginInProgressSession loginInProgressOwnerSession) {
    var key = loginInProgressOwnerSession.ownerSessionId();
    initialSessions.remove(key);
    inProgressSessions.put(key, loginInProgressOwnerSession);
    return loginInProgressOwnerSession;
  }

  @Override
  public LoggedInSession store(LoggedInSession loggedInOwnerSession) {
    var key = loggedInOwnerSession.ownerSessionId();
    inProgressSessions.remove(key);
    loggedInSessions.put(key, loggedInOwnerSession);
    return loggedInOwnerSession;
  }

  @Override
  public FinishedSession store(FinishedSession finishedOwnerSession) {
    var key = finishedOwnerSession.ownerSessionId();
    loggedInSessions.remove(key);
    finishedSessions.put(key, finishedOwnerSession);
    return finishedOwnerSession;
  }

  @Override
  public Optional<InitialSession> fetchInitialOwnerSession(OwnerSessionId ownerId) {
    return Optional.ofNullable(initialSessions.get(ownerId));
  }

  @Override
  public Optional<LoginInProgressSession> fetchLoginInProgressOwnerSession(OwnerSessionId ownerId) {
    return Optional.ofNullable(inProgressSessions.get(ownerId));
  }

  @Override
  public Optional<LoggedInSession> fetchLoggedInOwnerSession(OwnerSessionId ownerId) {
    return Optional.ofNullable(loggedInSessions.get(ownerId));
  }
}
