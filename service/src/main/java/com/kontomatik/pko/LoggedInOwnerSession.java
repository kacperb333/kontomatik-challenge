package com.kontomatik.pko;

import java.util.function.Consumer;

public record LoggedInOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId,
    PkoSessionId pkoSessionId
) {

    public FinishedOwnerSession doWithinSession(Consumer<LoggedInOwnerSession> toDo) {
        toDo.accept(this);
        return new FinishedOwnerSession(
            ownerSessionId,
            ownerId
        );
    }

    public LoggedInPkoSession asLoggedInPkoSession() {
        return new LoggedInPkoSession(pkoSessionId);
    }
}
