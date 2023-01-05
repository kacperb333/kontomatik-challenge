package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.Credentials;
import com.kontomatik.pko.domain.LoginInProgressPkoSession;
import com.kontomatik.pko.domain.Otp;
import com.kontomatik.pko.domain.PkoScraperFacade;
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

    //TODO handle not found session - create one in place?
    //TODO handle exceptions
    public LoginInProgressOwnerSession logIn(OwnerId ownerId, Credentials credentials) {
        var initialSession = ownerSessionRepository.fetchInitialOwnerSession(ownerId);
        var inProgressPkoSession = pkoScraperFacade.logIn(credentials);

        return ownerSessionRepository.store(new LoginInProgressOwnerSession(
            initialSession.ownerId(),
            inProgressPkoSession.sessionId(),
            inProgressPkoSession.flowId(),
            inProgressPkoSession.token()
        ));
    }

    //TODO handle not found session
    //TODO handle exceptions
    public LoggedInOwnerSession inputOtp(OwnerId ownerId, Otp otp) {
        var inProgressOwnerSession = ownerSessionRepository.fetchLoginInProgressOwnerSession(ownerId);
        var loggedInPkoSession = pkoScraperFacade.inputOtp(
            new LoginInProgressPkoSession(
                inProgressOwnerSession.sessionId(),
                inProgressOwnerSession.flowId(),
                inProgressOwnerSession.token()
            ),
            otp
        );

        return ownerSessionRepository.store(new LoggedInOwnerSession(
            inProgressOwnerSession.ownerId(),
            loggedInPkoSession.sessionId()
        ));
    }
}
