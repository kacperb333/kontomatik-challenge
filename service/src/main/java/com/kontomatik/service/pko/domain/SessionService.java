package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.signin.Credentials;
import com.kontomatik.lib.pko.domain.signin.Otp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
  private static final Logger log = LoggerFactory.getLogger(SessionService.class);
  private final PkoScraperFacade pkoScraperFacade;
  private final SessionRepository sessionRepository;
  private final AccountsImportScheduler accountsImportScheduler;

  SessionService(
    PkoScraperFacade pkoScraperFacade,
    SessionRepository sessionRepository,
    AccountsImportScheduler accountsImportScheduler
  ) {
    this.pkoScraperFacade = pkoScraperFacade;
    this.sessionRepository = sessionRepository;
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
      ImportFinishedSession finishedSession = importInProgressSession.finishSuccessful(accounts);
      sessionRepository.save(finishedSession);
    } catch (Exception e) {
      log.error("Encountered exception during import", e);
      ImportFailedSession finishedSession = importInProgressSession.finishFailed();
      sessionRepository.save(finishedSession);
    }
  }

  public AccountsImport getSessionAccountsImport(SessionId sessionId) {
    return sessionRepository.getSessionAccountsImport(sessionId);
  }
}
