package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.AccountsInfo;
import com.kontomatik.pko.domain.LoggedInPkoSession;
import com.kontomatik.pko.domain.PkoScraperFacade;
import org.springframework.stereotype.Service;

@Service
class OwnerAccountsService {

    private final PkoScraperFacade pkoScraperFacade;
    private final OwnerSessionRepository ownerSessionRepository;

    public OwnerAccountsService(PkoScraperFacade pkoScraperFacade, OwnerSessionRepository ownerSessionRepository) {
        this.pkoScraperFacade = pkoScraperFacade;
        this.ownerSessionRepository = ownerSessionRepository;
    }

    //TODO asynchronise
    public AccountsInfo fetchOwnerAccountsInfo(OwnerId ownerId) {
        return ownerSessionRepository.fetchLoggedInOwnerSession(ownerId)
            .map(loggedInOwnerSession -> pkoScraperFacade.fetchAccountsInfo(asLoggedInPkoSession(loggedInOwnerSession)))
            .orElseThrow(() -> new OwnerSessionNotLoggedIn(ownerId));
    }

    private static LoggedInPkoSession asLoggedInPkoSession(LoggedInOwnerSession loggedInOwnerSession) {
        return new LoggedInPkoSession(loggedInOwnerSession.sessionId());
    }
}
