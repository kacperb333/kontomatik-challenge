package com.kontomatik.pko;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerAccountsService {
    private static final Logger log = LoggerFactory.getLogger(OwnerAccountsService.class);

    private final PkoScraperFacade pkoScraperFacade;
    private final OwnerSessionService ownerSessionService;
    private final AccountsImportIdGenerator accountsImportIdGenerator;
    private final AccountsImportRepository accountImportRepository;
    private final AccountsImportScheduler accountsImportScheduler;
    private final DateTimeProvider dateTimeProvider;

    private static final Duration IMPORT_AVAILABILITY_DURATION = Duration.ofHours(24);

    public OwnerAccountsService(
        PkoScraperFacade pkoScraperFacade,
        OwnerSessionService ownerSessionService,
        AccountsImportIdGenerator accountsImportIdGenerator,
        AccountsImportRepository accountImportRepository,
        AccountsImportScheduler accountsImportScheduler,
        DateTimeProvider dateTimeProvider
    ) {
        this.pkoScraperFacade = pkoScraperFacade;
        this.ownerSessionService = ownerSessionService;
        this.accountsImportIdGenerator = accountsImportIdGenerator;
        this.accountImportRepository = accountImportRepository;
        this.accountsImportScheduler = accountsImportScheduler;
        this.dateTimeProvider = dateTimeProvider;
    }

    public ScheduledAccountsImport scheduleFetchOwnerAccountsInfo(OwnerSessionId ownerSessionId) {
        var accountsImportId = accountsImportIdGenerator.generate();
        ownerSessionService.doWithinOwnerSession(ownerSessionId, loggedInOwnerSession ->
            accountsImportScheduler.submitTask(() -> importAccounts(accountsImportId, loggedInOwnerSession))
        );
        return new ScheduledAccountsImport(ownerSessionId, accountsImportId);
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

    private void importAccounts(AccountsImportId accountsImportId, LoggedInOwnerSession loggedInOwnerSession) {
        try {
            markImportInProgress(accountsImportId, loggedInOwnerSession);
            var accountsInfo = pkoScraperFacade.fetchAccountsInfo(loggedInOwnerSession.asLoggedInPkoSession());
            markSuccessImport(accountsImportId, loggedInOwnerSession, accountsInfo);
        } catch (Exception e) {
            log.error("Encountered exception during import", e);
            markFailedImport(accountsImportId, loggedInOwnerSession, e);
        }
    }

    private void markImportInProgress(
        AccountsImportId accountsImportId,
        LoggedInOwnerSession loggedInOwnerSession
    ) {
        accountImportRepository.store(
            AccountsImport.inProgress(
                accountsImportId,
                loggedInOwnerSession.ownerId(),
                dateTimeProvider.now())
        );
    }

    private void markSuccessImport(
        AccountsImportId accountsImportId,
        LoggedInOwnerSession loggedInOwnerSession,
        AccountsInfo accountsInfo
    ) {
        accountImportRepository.store(
            AccountsImport.success(
                accountsImportId,
                loggedInOwnerSession.ownerId(),
                accountsInfo,
                dateTimeProvider.now()
            )
        );
    }

    private void markFailedImport(
        AccountsImportId accountsImportId,
        LoggedInOwnerSession loggedInOwnerSession,
        Exception e
    ) {
        accountImportRepository.store(
            AccountsImport.failure(
                accountsImportId,
                loggedInOwnerSession.ownerId(),
                dateTimeProvider.now(),
                AccountsImport.Details.ofMessage(e.getMessage())
            )
        );
    }
}
