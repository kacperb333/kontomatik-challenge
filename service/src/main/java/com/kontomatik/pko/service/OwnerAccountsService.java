package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.AccountsInfo;
import com.kontomatik.pko.domain.LoggedInPkoSession;
import com.kontomatik.pko.domain.PkoScraperFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executors;

@Service
class OwnerAccountsService {

    private final PkoScraperFacade pkoScraperFacade;
    private final AccountsImportIdGenerator accountsImportIdGenerator;
    private final OwnerSessionRepository ownerSessionRepository;
    private final AccountsImportRepository accountImportRepository;

    public OwnerAccountsService(
        PkoScraperFacade pkoScraperFacade,
        AccountsImportIdGenerator accountsImportIdGenerator,
        OwnerSessionRepository ownerSessionRepository,
        AccountsImportRepository accountImportRepository
    ) {
        this.pkoScraperFacade = pkoScraperFacade;
        this.accountsImportIdGenerator = accountsImportIdGenerator;
        this.ownerSessionRepository = ownerSessionRepository;
        this.accountImportRepository = accountImportRepository;
    }

    public ScheduledAccountsImport scheduleFetchOwnerAccountsInfo(OwnerSessionId ownerSessionId) {
        return ownerSessionRepository.fetchLoggedInOwnerSession(ownerSessionId)
            .map(this::doScheduleForOwner)
            .orElseThrow(() -> new OwnerSessionNotLoggedIn(ownerSessionId));
    }

    public Optional<AccountsInfo> fetchSingleImport(AccountsImportId accountsImportId) {
        return accountImportRepository.fetch(accountsImportId)
            .map(AccountsImport::accountsInfo);
    }

    public Optional<AccountsInfo> fetchAllImportsForOwner(OwnerId ownerId) {
        var mergedAccountInfos = accountImportRepository.fetchAll(ownerId).stream()
            .flatMap(it -> it.accountsInfo().accounts().stream())
            .toList();

        return Optional.of(new AccountsInfo(mergedAccountInfos));
    }

    private ScheduledAccountsImport doScheduleForOwner(LoggedInOwnerSession loggedInOwnerSession) {
        var accountsImportId = accountsImportIdGenerator.generate();
        Runnable importTask = () -> {
            var accountsInfo = pkoScraperFacade.fetchAccountsInfo(asLoggedInPkoSession(loggedInOwnerSession));
            var importResult = new AccountsImport(
                accountsImportId,
                loggedInOwnerSession.ownerId(),
                accountsInfo
            );
            accountImportRepository.store(importResult);
        };
        submitTask(importTask);

        return new ScheduledAccountsImport(loggedInOwnerSession.ownerSessionId(), accountsImportId);
    }

    private static LoggedInPkoSession asLoggedInPkoSession(LoggedInOwnerSession loggedInOwnerSession) {
        return new LoggedInPkoSession(loggedInOwnerSession.pkoSessionId());
    }

    private static void submitTask(Runnable task) {
        Executors.newSingleThreadExecutor().submit(task);
    }
}
