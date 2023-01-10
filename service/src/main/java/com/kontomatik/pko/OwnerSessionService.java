package com.kontomatik.pko;

import org.springframework.stereotype.Service;

@Service
public class OwnerSessionService {

    private final PkoScraperFacade pkoScraperFacade;
    private final OwnerSessionIdGenerator ownerSessionIdGenerator;
    private final OwnerSessionRepository ownerSessionRepository;

    public OwnerSessionService(
        PkoScraperFacade pkoScraperFacade,
        OwnerSessionIdGenerator ownerSessionIdGenerator,
        OwnerSessionRepository ownerSessionRepository
    ) {
        this.pkoScraperFacade = pkoScraperFacade;
        this.ownerSessionIdGenerator = ownerSessionIdGenerator;
        this.ownerSessionRepository = ownerSessionRepository;
    }

    public InitialOwnerSession initializeOwnerSession(OwnerId ownerId) {
        var generatedSessionId = ownerSessionIdGenerator.generate();
        return ownerSessionRepository.store(new InitialOwnerSession(generatedSessionId, ownerId));
    }

    public LoginInProgressOwnerSession logIn(OwnerSessionId ownerSessionId, Credentials credentials) {
        var loginInProgressSession = ownerSessionRepository.fetchInitialOwnerSession(ownerSessionId)
            .map(initialOwnerSession -> {
                var inProgressPkoSession = pkoScraperFacade.logIn(credentials);
                return asLogInInProgressOwnerSession(inProgressPkoSession, initialOwnerSession);
            })
            .orElseThrow(() -> new OwnerSessionNotInitialized(ownerSessionId));

        return ownerSessionRepository.store(loginInProgressSession);
    }

    public LoggedInOwnerSession inputOtp(OwnerSessionId ownerSessionId, Otp otp) {
        var loggedInSession = ownerSessionRepository.fetchLoginInProgressOwnerSession(ownerSessionId)
            .map(inProgressOwnerSession -> {
                var loggedInPkoSession = pkoScraperFacade.inputOtp(
                    asLoginInProgressPkoSession(inProgressOwnerSession),
                    otp
                );
                return asLoggedInOwnerSession(loggedInPkoSession, inProgressOwnerSession);
            })
            .orElseThrow(() -> new OwnerSessionLoginNotInProgress(ownerSessionId));

        return ownerSessionRepository.store(loggedInSession);
    }

    private static LoginInProgressOwnerSession asLogInInProgressOwnerSession(
        LoginInProgressPkoSession inProgressPkoSession,
        InitialOwnerSession initialOwnerSession
    ) {
        return new LoginInProgressOwnerSession(
            initialOwnerSession.ownerSessionId(),
            initialOwnerSession.ownerId(),
            inProgressPkoSession.pkoSessionId(),
            inProgressPkoSession.flowId(),
            inProgressPkoSession.token()
        );
    }

    private static LoginInProgressPkoSession asLoginInProgressPkoSession(
        LoginInProgressOwnerSession inProgressOwnerSession
    ) {
        return new LoginInProgressPkoSession(
            inProgressOwnerSession.pkoSessionId(),
            inProgressOwnerSession.flowId(),
            inProgressOwnerSession.token()
        );
    }

    private static LoggedInOwnerSession asLoggedInOwnerSession(
        LoggedInPkoSession loggedInPkoSession,
        LoginInProgressOwnerSession inProgressOwnerSession
    ) {
        return new LoggedInOwnerSession(
            inProgressOwnerSession.ownerSessionId(),
            inProgressOwnerSession.ownerId(),
            loggedInPkoSession.pkoSessionId()
        );
    }
}
