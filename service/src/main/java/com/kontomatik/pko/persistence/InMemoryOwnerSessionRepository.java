package com.kontomatik.pko.persistence;

import com.kontomatik.pko.service.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//TODO rework maybe? Clear previous session state?
@Component
class InMemoryOwnerSessionRepository implements OwnerSessionRepository {

    private final ConcurrentMap<OwnerId, InitialOwnerSession> initialSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<OwnerId, LoginInProgressOwnerSession> inProgressSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<OwnerId, LoggedInOwnerSession> loggedInSessions = new ConcurrentHashMap<>();

    @Override
    public InitialOwnerSession store(InitialOwnerSession initialOwnerSession) {
        return initialSessions.put(initialOwnerSession.ownerId(), initialOwnerSession);
    }

    @Override
    public LoginInProgressOwnerSession store(LoginInProgressOwnerSession loginInProgressOwnerSession) {
        return inProgressSessions.put(loginInProgressOwnerSession.ownerId(), loginInProgressOwnerSession);
    }

    @Override
    public LoggedInOwnerSession store(LoggedInOwnerSession loggedInOwnerSession) {
        return loggedInSessions.put(loggedInOwnerSession.ownerId(), loggedInOwnerSession);
    }

    @Override
    public InitialOwnerSession fetchInitialOwnerSession(OwnerId ownerId) {
        return initialSessions.get(ownerId);
    }

    @Override
    public LoginInProgressOwnerSession fetchLoginInProgressOwnerSession(OwnerId ownerId) {
        return inProgressSessions.get(ownerId);
    }

    @Override
    public LoggedInOwnerSession fetchLoggedInOwnerSession(OwnerId ownerId) {
        return loggedInSessions.get(ownerId);
    }
}
