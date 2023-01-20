package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.domain.accounts.AccountsInfo;
import com.kontomatik.lib.pko.domain.login.Credentials;
import com.kontomatik.lib.pko.domain.login.Otp;
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

  public LoginInProgressSession logIn(Credentials credentials) {
    LoginInProgressSession loginInProgressSession = new LoginInProgressSession(
      SessionIdGenerator.generate(),
      pkoScraperFacade.logIn(credentials)
    );
    return sessionRepository.save(loginInProgressSession);
  }

  public ImportInProgressSession inputOtp(SessionId sessionId, Otp otp) {
    LoginInProgressSession loginInProgressSession = sessionRepository.getLoginInProgressSession(sessionId);
    ImportInProgressSession importInProgressSession = loginInProgressSession.finishLogin(
      pkoScraperFacade.inputOtp(
        loginInProgressSession.pkoSession(),
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
      AccountsInfo accountsInfo = pkoScraperFacade.fetchAccountsInfo(importInProgressSession.pkoSession());
      ImportFinishedSession finishedSession = importInProgressSession.finishSuccessful(
        accountsInfo
      );
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
