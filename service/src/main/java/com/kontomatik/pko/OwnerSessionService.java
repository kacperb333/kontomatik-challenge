package com.kontomatik.pko;

import org.springframework.stereotype.Service;

import java.util.function.Consumer;

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
        ownerSessionRepository.fetchInitialOwnerSession(ownerSessionId);
        var loginInProgressSession = ownerSessionRepository.fetchInitialOwnerSession(ownerSessionId)
            .map(initialOwnerSession -> initialOwnerSession.initializeLogIn(pkoScraperFacade.logIn(credentials)))
            .orElseThrow(() -> new OwnerSessionNotInitialized(ownerSessionId));

        return ownerSessionRepository.store(loginInProgressSession);
    }

    public LoggedInOwnerSession inputOtp(OwnerSessionId ownerSessionId, Otp otp) {
        var loggedInSession = ownerSessionRepository.fetchLoginInProgressOwnerSession(ownerSessionId)
            .map(inProgressOwnerSession ->
                inProgressOwnerSession.finishLogin(pkoScraperFacade.inputOtp(
                    inProgressOwnerSession.asLoginInProgressPkoSession(),
                    otp
                ))
            )
            .orElseThrow(() -> new OwnerSessionLoginNotInProgress(ownerSessionId));

        return ownerSessionRepository.store(loggedInSession);
    }

    public void doWithinOwnerSession(OwnerSessionId ownerSessionId, Consumer<LoggedInOwnerSession> sessionConsumer) {
        var loggedInOwnerSession = ownerSessionRepository.fetchLoggedInOwnerSession(ownerSessionId)
            .orElseThrow(() -> new OwnerSessionNotLoggedIn(ownerSessionId));

        var finishedSession = loggedInOwnerSession.doWithinSession(sessionConsumer);

        ownerSessionRepository.store(finishedSession);
    }
}
