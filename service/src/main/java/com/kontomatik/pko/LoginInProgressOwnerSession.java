package com.kontomatik.pko;

public record LoginInProgressOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId,
    PkoSessionId pkoSessionId,
    FlowId flowId,
    Token token
) {
    public LoggedInOwnerSession finishLogin(LoggedInPkoSession loggedInPkoSession) {
        return new LoggedInOwnerSession(
            ownerSessionId,
            ownerId,
            loggedInPkoSession.pkoSessionId()
        );
    }

    public LoginInProgressPkoSession asLoginInProgressPkoSession() {
        return new LoginInProgressPkoSession(
            pkoSessionId,
            flowId,
            token
        );
    }
}
