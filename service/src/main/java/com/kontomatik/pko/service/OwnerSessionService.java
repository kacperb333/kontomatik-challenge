package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.*;
import org.springframework.stereotype.Service;

@Service
class OwnerSessionService {

    private final PkoScraperFacade pkoScraperFacade;
    private final OwnerSessionRepository ownerSessionRepository;

    public OwnerSessionService(PkoScraperFacade pkoScraperFacade, OwnerSessionRepository ownerSessionRepository) {
        this.pkoScraperFacade = pkoScraperFacade;
        this.ownerSessionRepository = ownerSessionRepository;
    }

    public InitialOwnerSession initializeOwnerSession(OwnerId ownerId) {
        return ownerSessionRepository.store(new InitialOwnerSession(ownerId));
    }

    public LoginInProgressOwnerSession logIn(OwnerId ownerId, Credentials credentials) {
        var loginInProgressSession = ownerSessionRepository.fetchInitialOwnerSession(ownerId)
            .map(initialOwnerSession -> {
                var inProgressPkoSession = pkoScraperFacade.logIn(credentials);
                return asLogInInProgressOwnerSession(inProgressPkoSession, initialOwnerSession);
            })
            .orElseThrow(() -> new OwnerSessionNotInitialized(ownerId));

        return ownerSessionRepository.store(loginInProgressSession);
    }

    public LoggedInOwnerSession inputOtp(OwnerId ownerId, Otp otp) {
        var loggedInSession = ownerSessionRepository.fetchLoginInProgressOwnerSession(ownerId)
            .map(inProgressOwnerSession -> {
                var loggedInPkoSession = pkoScraperFacade.inputOtp(
                    asLoginInProgressPkoSession(inProgressOwnerSession),
                    otp
                );
                return asLoggedInOwnerSession(loggedInPkoSession, inProgressOwnerSession);
            })
            .orElseThrow(() -> new OwnerSessionLoginNotInProgress(ownerId));

        return ownerSessionRepository.store(loggedInSession);
    }

    private static LoginInProgressOwnerSession asLogInInProgressOwnerSession(
        LoginInProgressPkoSession inProgressPkoSession,
        InitialOwnerSession initialOwnerSession
    ) {
        return new LoginInProgressOwnerSession(
            initialOwnerSession.ownerId(),
            inProgressPkoSession.sessionId(),
            inProgressPkoSession.flowId(),
            inProgressPkoSession.token()
        );
    }

    private static LoginInProgressPkoSession asLoginInProgressPkoSession(
        LoginInProgressOwnerSession inProgressOwnerSession
    ) {
        return new LoginInProgressPkoSession(
            inProgressOwnerSession.sessionId(),
            inProgressOwnerSession.flowId(),
            inProgressOwnerSession.token()
        );
    }

    private static LoggedInOwnerSession asLoggedInOwnerSession(
        LoggedInPkoSession loggedInPkoSession,
        LoginInProgressOwnerSession inProgressOwnerSession
    ) {
        return new LoggedInOwnerSession(
            inProgressOwnerSession.ownerId(),
            loggedInPkoSession.sessionId()
        );
    }
}
