package com.kontomatik.pko;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerAccountsService {

    private final PkoScraperFacade pkoScraperFacade;
    private final AccountsImportIdGenerator accountsImportIdGenerator;
    private final OwnerSessionRepository ownerSessionRepository;
    private final AccountsImportRepository accountImportRepository;
    private final AccountsImportScheduler accountsImportScheduler;
    private final DateTimeProvider dateTimeProvider;

    private static final Duration IMPORT_AVAILABILITY_DURATION = Duration.ofHours(24);

    public OwnerAccountsService(
        PkoScraperFacade pkoScraperFacade,
        AccountsImportIdGenerator accountsImportIdGenerator,
        OwnerSessionRepository ownerSessionRepository,
        AccountsImportRepository accountImportRepository,
        AccountsImportScheduler accountsImportScheduler,
        DateTimeProvider dateTimeProvider
    ) {
        this.pkoScraperFacade = pkoScraperFacade;
        this.accountsImportIdGenerator = accountsImportIdGenerator;
        this.ownerSessionRepository = ownerSessionRepository;
        this.accountImportRepository = accountImportRepository;
        this.accountsImportScheduler = accountsImportScheduler;
        this.dateTimeProvider = dateTimeProvider;
    }

    public ScheduledAccountsImport scheduleFetchOwnerAccountsInfo(OwnerSessionId ownerSessionId) {
        return ownerSessionRepository.fetchLoggedInOwnerSession(ownerSessionId)
            .map(this::doScheduleForOwner)
            .orElseThrow(() -> new OwnerSessionNotLoggedIn(ownerSessionId));
    }

    public Optional<AccountsImport> fetchSingleImport(AccountsImportId accountsImportId) {
        var importMaxTime = dateTimeProvider.now().minus(IMPORT_AVAILABILITY_DURATION);
        return accountImportRepository.fetchOneNewerThan(accountsImportId, importMaxTime);
    }

    public List<AccountsImport> fetchAllImportsForOwner(OwnerId ownerId) {
        var importMaxTime = dateTimeProvider.now().minus(IMPORT_AVAILABILITY_DURATION);
        return accountImportRepository.fetchAllForOwnerNewerThan(ownerId, importMaxTime).stream()
            .toList();
    }

    private ScheduledAccountsImport doScheduleForOwner(LoggedInOwnerSession loggedInOwnerSession) {
        var accountsImportId = accountsImportIdGenerator.generate();
        Runnable importTask = () -> importAccounts(accountsImportId, loggedInOwnerSession);
        accountsImportScheduler.submitTask(importTask);

        return new ScheduledAccountsImport(loggedInOwnerSession.ownerSessionId(), accountsImportId);
    }

    private void importAccounts(AccountsImportId accountsImportId, LoggedInOwnerSession loggedInOwnerSession) {
        try {
            markImportInProgress(accountsImportId, loggedInOwnerSession);

            Thread.sleep(20000);

            var accountsInfo = pkoScraperFacade.fetchAccountsInfo(asLoggedInPkoSession(loggedInOwnerSession));
            var importResult = AccountsImport.success(
                accountsImportId,
                loggedInOwnerSession.ownerId(),
                accountsInfo,
                dateTimeProvider.now()
            );
            accountImportRepository.store(importResult);
        } catch (Exception e) {
            //TODO logging
            System.out.println(e.getMessage());
            var failedResult = AccountsImport.failure(accountsImportId,
                loggedInOwnerSession.ownerId(),
                dateTimeProvider.now()
            );
            accountImportRepository.store(failedResult);
        }
    }

    private void markImportInProgress(AccountsImportId accountsImportId, LoggedInOwnerSession loggedInOwnerSession) {
        accountImportRepository.store(
            AccountsImport.inProgress(accountsImportId, loggedInOwnerSession.ownerId(), dateTimeProvider.now()));
    }

    private static LoggedInPkoSession asLoggedInPkoSession(LoggedInOwnerSession loggedInOwnerSession) {
        return new LoggedInPkoSession(loggedInOwnerSession.pkoSessionId());
    }
}
