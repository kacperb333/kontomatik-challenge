package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.signin.Credentials;
import com.kontomatik.lib.pko.domain.signin.Otp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionService {
  private static final Logger log = LoggerFactory.getLogger(SessionService.class);
  private final PkoScraperFacade pkoScraperFacade;
  private final SessionRepository sessionRepository;
  private final AccountsImportRepository accountsImportRepository;
  private final AccountsImportScheduler accountsImportScheduler;

  SessionService(
    PkoScraperFacade pkoScraperFacade,
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
      SuccessfulImport finishedSession = importInProgressSession.finishSuccessful(accounts);
      accountsImportRepository.save(finishedSession);
    } catch (Exception e) {
      log.error("Encountered exception during import", e);
      FailedImport finishedSession = importInProgressSession.finishFailed();
      accountsImportRepository.save(finishedSession);
    }
  }

  public Optional<FinishedImport> findSessionAccountsImport(SessionId sessionId) {
    return accountsImportRepository.findAccountsImport(sessionId);
  }
}
