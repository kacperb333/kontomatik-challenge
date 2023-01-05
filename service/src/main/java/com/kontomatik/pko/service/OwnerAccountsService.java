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

    //TODO handle not found session
    //TODO handle exceptions
    //TODO asynchronise
    public AccountsInfo fetchOwnerAccountsInfo(OwnerId ownerId) {
        var loggedInOwnerSession = ownerSessionRepository.fetchLoggedInOwnerSession(ownerId);
        return pkoScraperFacade.fetchAccountsInfo(
            new LoggedInPkoSession(loggedInOwnerSession.sessionId())
        );
    }
}
