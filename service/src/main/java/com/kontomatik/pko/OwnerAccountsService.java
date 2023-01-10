package com.kontomatik.pko;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;

@Service
public class OwnerAccountsService {

    private final PkoScraperFacade pkoScraperFacade;
    private final AccountsImportIdGenerator accountsImportIdGenerator;
    private final OwnerSessionRepository ownerSessionRepository;
    private final AccountsImportRepository accountImportRepository;
    private final DateTimeProvider dateTimeProvider;

    private static final Duration IMPORT_AVAILABILITY_DURATION = Duration.ofHours(24);

    public OwnerAccountsService(
        PkoScraperFacade pkoScraperFacade,
        AccountsImportIdGenerator accountsImportIdGenerator,
        OwnerSessionRepository ownerSessionRepository,
        AccountsImportRepository accountImportRepository,
        DateTimeProvider dateTimeProvider
    ) {
        this.pkoScraperFacade = pkoScraperFacade;
        this.accountsImportIdGenerator = accountsImportIdGenerator;
        this.ownerSessionRepository = ownerSessionRepository;
        this.accountImportRepository = accountImportRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    public ScheduledAccountsImport scheduleFetchOwnerAccountsInfo(OwnerSessionId ownerSessionId) {
        return ownerSessionRepository.fetchLoggedInOwnerSession(ownerSessionId)
            .map(this::doScheduleForOwner)
            .orElseThrow(() -> new OwnerSessionNotLoggedIn(ownerSessionId));
    }

    public Optional<AccountsInfo> fetchSingleImport(AccountsImportId accountsImportId) {
        var importMaxTime = dateTimeProvider.now().minus(IMPORT_AVAILABILITY_DURATION);
        return accountImportRepository.fetchNewerThan(accountsImportId, importMaxTime)
            .map(AccountsImport::accountsInfo);
    }

    public Optional<AccountsInfo> fetchAllImportsForOwner(OwnerId ownerId) {
        var importMaxTime = dateTimeProvider.now().minus(IMPORT_AVAILABILITY_DURATION);
        var mergedAccountInfos = accountImportRepository.fetchAllNewerThan(ownerId, importMaxTime).stream()
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
                accountsInfo,
                dateTimeProvider.now()
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
