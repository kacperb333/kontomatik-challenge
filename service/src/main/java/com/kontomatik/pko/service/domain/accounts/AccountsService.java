package com.kontomatik.pko.service.domain.accounts;

import com.kontomatik.pko.lib.PkoScraperFacade;
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.service.DateTimeProvider;
import com.kontomatik.pko.service.domain.session.LoggedInSession;
import com.kontomatik.pko.service.domain.session.SessionId;
import com.kontomatik.pko.service.domain.session.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class AccountsService {
  private static final Logger log = LoggerFactory.getLogger(AccountsService.class);

  private final PkoScraperFacade pkoScraperFacade;
  private final SessionService SessionService;
  private final AccountsImportRepository accountImportRepository;
  private final AccountsImportScheduler accountsImportScheduler;
  private final DateTimeProvider dateTimeProvider;

  private static final Duration IMPORT_AVAILABILITY_DURATION = Duration.ofHours(24);

  AccountsService(
    PkoScraperFacade pkoScraperFacade,
    SessionService sessionService,
    AccountsImportRepository accountImportRepository,
    AccountsImportScheduler accountsImportScheduler,
    DateTimeProvider dateTimeProvider
  ) {
    this.pkoScraperFacade = pkoScraperFacade;
    this.SessionService = sessionService;
    this.accountImportRepository = accountImportRepository;
    this.accountsImportScheduler = accountsImportScheduler;
    this.dateTimeProvider = dateTimeProvider;
  }

  public ScheduledAccountsImport scheduleFetchAccountsInfo(SessionId sessionId) {
    var accountsImportId = AccountsImportIdGenerator.generate();
    SessionService.doWithinSession(sessionId, loggedInSession ->
      accountsImportScheduler.submitTask(() -> importAccounts(accountsImportId, loggedInSession))
    );
    return new ScheduledAccountsImport(sessionId, accountsImportId);
  }

  public Optional<AccountsImport> fetchSingleImport(AccountsImportId accountsImportId) {
    var importMaxTime = dateTimeProvider.now().minus(IMPORT_AVAILABILITY_DURATION);
    return accountImportRepository.findOneNewerThan(accountsImportId, importMaxTime);
  }

  private void importAccounts(AccountsImportId accountsImportId, LoggedInSession loggedInSession) {
    try {
      markImportInProgress(accountsImportId);
      var accountsInfo = pkoScraperFacade.fetchAccountsInfo(loggedInSession.asLoggedInPkoSession());
      markSuccessImport(accountsImportId, accountsInfo);
    } catch (Exception e) {
      log.error("Encountered exception during import", e);
      markFailedImport(accountsImportId, e);
    }
  }

  private void markImportInProgress(AccountsImportId accountsImportId) {
    accountImportRepository.save(
      AccountsImport.inProgress(
        accountsImportId,
        dateTimeProvider.now())
    );
  }

  private void markSuccessImport(AccountsImportId accountsImportId, AccountsInfo accountsInfo) {
    accountImportRepository.save(
      AccountsImport.success(
        accountsImportId,
        accountsInfo,
        dateTimeProvider.now()
      )
    );
  }

  private void markFailedImport(AccountsImportId accountsImportId, Exception e) {
    accountImportRepository.save(
      AccountsImport.failure(
        accountsImportId,
        dateTimeProvider.now(),
        AccountsImport.Details.ofMessage(e.getMessage())
      )
    );
  }
}
