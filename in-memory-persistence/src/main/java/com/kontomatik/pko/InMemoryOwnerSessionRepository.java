package com.kontomatik.pko;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemoryOwnerSessionRepository implements OwnerSessionRepository {

    private final ConcurrentMap<OwnerSessionId, InitialOwnerSession> initialSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<OwnerSessionId, LoginInProgressOwnerSession> inProgressSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<OwnerSessionId, LoggedInOwnerSession> loggedInSessions = new ConcurrentHashMap<>();

    @Override
    public InitialOwnerSession store(InitialOwnerSession initialOwnerSession) {
        initialSessions.put(initialOwnerSession.ownerSessionId(), initialOwnerSession);
        return initialOwnerSession;
    }

    @Override
    public LoginInProgressOwnerSession store(LoginInProgressOwnerSession loginInProgressOwnerSession) {
        var key = loginInProgressOwnerSession.ownerSessionId();
        initialSessions.remove(key);
        inProgressSessions.put(key, loginInProgressOwnerSession);
        return loginInProgressOwnerSession;
    }

    @Override
    public LoggedInOwnerSession store(LoggedInOwnerSession loggedInOwnerSession) {
        var key = loggedInOwnerSession.ownerSessionId();
        inProgressSessions.remove(key);
        loggedInSessions.put(key, loggedInOwnerSession);
        return loggedInOwnerSession;
    }

    @Override
    public Optional<InitialOwnerSession> fetchInitialOwnerSession(OwnerSessionId ownerId) {
        return Optional.ofNullable(initialSessions.get(ownerId));
    }

    @Override
    public Optional<LoginInProgressOwnerSession> fetchLoginInProgressOwnerSession(OwnerSessionId ownerId) {
        return Optional.ofNullable(inProgressSessions.get(ownerId));
    }

    @Override
    public Optional<LoggedInOwnerSession> fetchLoggedInOwnerSession(OwnerSessionId ownerId) {
        return Optional.ofNullable(loggedInSessions.get(ownerId));
    }
}
