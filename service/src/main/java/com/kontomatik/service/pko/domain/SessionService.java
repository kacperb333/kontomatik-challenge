package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.ScraperFacade;
import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.signin.Credentials;
import com.kontomatik.lib.pko.domain.signin.Otp;
import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionService {
  private static final Logger log = LoggerFactory.getLogger(SessionService.class);
  private final ScraperFacade pkoScraperFacade;
  private final SessionRepository sessionRepository;
  private final AccountsImportRepository accountsImportRepository;
  private final AccountsImportScheduler accountsImportScheduler;

  SessionService(
    ScraperFacade pkoScraperFacade,
    SessionRepository sessionRepository,
    AccountsImportRepository accountsImportRepository,
    AccountsImportScheduler accountsImportScheduler
  ) {
    this.pkoScraperFacade = pkoScraperFacade;
    this.sessionRepository = sessionRepository;
    this.accountsImportRepository = accountsImportRepository;
    this.accountsImportScheduler = accountsImportScheduler;
  }

  public OtpRequiredSession signIn(Credentials credentials) {
    OtpRequiredSession otpRequiredSession = new OtpRequiredSession(
      SessionIdGenerator.generate(),
      pkoScraperFacade.signIn(credentials)
    );
    return sessionRepository.save(otpRequiredSession);
  }

  public ImportInProgressSession inputOtp(SessionId sessionId, Otp otp) {
    OtpRequiredSession otpRequiredSession = sessionRepository.getOtpRequiredSession(sessionId);
    ImportInProgressSession importInProgressSession = otpRequiredSession.finishSignIn(
      pkoScraperFacade.inputOtp(
        otpRequiredSession.pkoSession(),
        otp
      )
    );
    scheduleAccountsImport(importInProgressSession);
    return importInProgressSession;
  }

  private void scheduleAccountsImport(ImportInProgressSession importInProgressSession) {
    Runnable importTask = () -> importAccounts(importInProgressSession);
    accountsImportScheduler.schedule(importTask);
  }

  private void importAccounts(ImportInProgressSession importInProgressSession) {
    try {
      Accounts accounts = pkoScraperFacade.fetchAccounts(importInProgressSession.pkoSession());
      SuccessfulImport successfulImport = importInProgressSession.finishSuccessful(accounts);
      accountsImportRepository.save(successfulImport);
    } catch (Exception e) {
      log.error("Encountered exception during import", e);
      FailedImport failedImport = importInProgressSession.finishFailed();
      accountsImportRepository.save(failedImport);
    }
  }

  public Optional<FinishedImport> findSessionAccountsImport(SessionId sessionId) {
    return accountsImportRepository.findAccountsImport(sessionId);
  }
}
