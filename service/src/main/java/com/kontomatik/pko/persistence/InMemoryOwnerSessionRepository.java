package com.kontomatik.pko.persistence;

import com.kontomatik.pko.service.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemoryOwnerSessionRepository implements OwnerSessionRepository {

    private final ConcurrentMap<OwnerId, InitialOwnerSession> initialSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<OwnerId, LoginInProgressOwnerSession> inProgressSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<OwnerId, LoggedInOwnerSession> loggedInSessions = new ConcurrentHashMap<>();

    @Override
    public InitialOwnerSession store(InitialOwnerSession initialOwnerSession) {
        initialSessions.put(initialOwnerSession.ownerId(), initialOwnerSession);
        return initialOwnerSession;
    }

    @Override
    public LoginInProgressOwnerSession store(LoginInProgressOwnerSession loginInProgressOwnerSession) {
        var key = loginInProgressOwnerSession.ownerId();
        initialSessions.remove(key);
        inProgressSessions.put(key, loginInProgressOwnerSession);
        return loginInProgressOwnerSession;
    }

    @Override
    public LoggedInOwnerSession store(LoggedInOwnerSession loggedInOwnerSession) {
        var key = loggedInOwnerSession.ownerId();
        inProgressSessions.remove(key);
        loggedInSessions.put(key, loggedInOwnerSession);
        return loggedInOwnerSession;
    }

    @Override
    public Optional<InitialOwnerSession> fetchInitialOwnerSession(OwnerId ownerId) {
        return Optional.ofNullable(initialSessions.get(ownerId));
    }

    @Override
    public Optional<LoginInProgressOwnerSession> fetchLoginInProgressOwnerSession(OwnerId ownerId) {
        return Optional.ofNullable(inProgressSessions.get(ownerId));
    }

    @Override
    public Optional<LoggedInOwnerSession> fetchLoggedInOwnerSession(OwnerId ownerId) {
        return Optional.ofNullable(loggedInSessions.get(ownerId));
    }
}
