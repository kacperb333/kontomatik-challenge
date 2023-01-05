package com.kontomatik.pko.service;

public interface OwnerSessionRepository {
    InitialOwnerSession store(InitialOwnerSession initialOwnerSession);

    LoginInProgressOwnerSession store(LoginInProgressOwnerSession loginInProgressOwnerSession);

    LoggedInOwnerSession store(LoggedInOwnerSession loggedInOwnerSession);

    InitialOwnerSession fetchInitialOwnerSession(OwnerId ownerId);

    LoginInProgressOwnerSession fetchLoginInProgressOwnerSession(OwnerId ownerId);

    LoggedInOwnerSession fetchLoggedInOwnerSession(OwnerId ownerId);
}
