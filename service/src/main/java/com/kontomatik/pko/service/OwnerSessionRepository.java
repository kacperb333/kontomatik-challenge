package com.kontomatik.pko.service;

import java.util.Optional;

public interface OwnerSessionRepository {
    InitialOwnerSession store(InitialOwnerSession initialOwnerSession);

    LoginInProgressOwnerSession store(LoginInProgressOwnerSession loginInProgressOwnerSession);

    LoggedInOwnerSession store(LoggedInOwnerSession loggedInOwnerSession);

    Optional<InitialOwnerSession> fetchInitialOwnerSession(OwnerId ownerId);

    Optional<LoginInProgressOwnerSession> fetchLoginInProgressOwnerSession(OwnerId ownerId);

    Optional<LoggedInOwnerSession> fetchLoggedInOwnerSession(OwnerId ownerId);
}
