package com.kontomatik.pko;

public record InitialOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId
) {
    public LoginInProgressOwnerSession initializeLogIn(LoginInProgressPkoSession loginInProgressPkoSession) {
        return new LoginInProgressOwnerSession(
            ownerSessionId,
            ownerId,
            loginInProgressPkoSession.pkoSessionId(),
            loginInProgressPkoSession.flowId(),
            loginInProgressPkoSession.token()
        );
    }
}
