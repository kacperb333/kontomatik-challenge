package com.kontomatik.pko;

import java.util.Optional;

public interface OwnerSessionRepository {
    InitialOwnerSession store(InitialOwnerSession initialOwnerSession);

    LoginInProgressOwnerSession store(LoginInProgressOwnerSession loginInProgressOwnerSession);

    LoggedInOwnerSession store(LoggedInOwnerSession loggedInOwnerSession);

    FinishedOwnerSession store(FinishedOwnerSession finishedOwnerSession);

    Optional<InitialOwnerSession> fetchInitialOwnerSession(OwnerSessionId ownerId);

    Optional<LoginInProgressOwnerSession> fetchLoginInProgressOwnerSession(OwnerSessionId ownerId);

    Optional<LoggedInOwnerSession> fetchLoggedInOwnerSession(OwnerSessionId ownerId);
}
