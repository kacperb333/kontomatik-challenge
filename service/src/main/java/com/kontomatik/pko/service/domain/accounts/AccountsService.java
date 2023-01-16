package com.kontomatik.pko.service.domain.accounts;

import com.kontomatik.pko.lib.PkoScraperFacade;
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.service.DateTimeProvider;
import com.kontomatik.pko.service.domain.session.LoggedInSession;
import com.kontomatik.pko.service.domain.session.OwnerId;
import com.kontomatik.pko.service.domain.session.OwnerSessionId;
import com.kontomatik.pko.service.domain.session.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class AccountsService {
  private static final Logger log = LoggerFactory.getLogger(AccountsService.class);

  private final PkoScraperFacade pkoScraperFacade;
  private final SessionService ownerSessionService;
  private final AccountsImportRepository accountImportRepository;
  private final AccountsImportScheduler accountsImportScheduler;
  private final DateTimeProvider dateTimeProvider;

  private static final Duration IMPORT_AVAILABILITY_DURATION = Duration.ofHours(24);

  AccountsService(
    PkoScraperFacade pkoScraperFacade,
    SessionService ownerSessionService,
    AccountsImportRepository accountImportRepository,
    AccountsImportScheduler accountsImportScheduler,
    DateTimeProvider dateTimeProvider
  ) {
    this.pkoScraperFacade = pkoScraperFacade;
    this.ownerSessionService = ownerSessionService;
    this.accountImportRepository = accountImportRepository;
    this.accountsImportScheduler = accountsImportScheduler;
    this.dateTimeProvider = dateTimeProvider;
  }

  public ScheduledAccountsImport scheduleFetchOwnerAccountsInfo(OwnerSessionId ownerSessionId) {
    var accountsImportId = AccountsImportIdGenerator.generate();
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

  private void importAccounts(AccountsImportId accountsImportId, LoggedInSession loggedInOwnerSession) {
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
    LoggedInSession loggedInOwnerSession
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
    LoggedInSession loggedInOwnerSession,
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
    LoggedInSession loggedInOwnerSession,
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
