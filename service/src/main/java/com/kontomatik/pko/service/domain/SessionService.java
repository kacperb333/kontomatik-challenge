package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.PkoScraperFacade;
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.lib.usecase.login.Credentials;
import com.kontomatik.pko.lib.usecase.login.Otp;
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
    LoginInProgressSession loginInProgressSession = LoginInProgressSession.from(
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
      FinishedSession finishedSession = importInProgressSession.finishSuccessful(
        accountsInfo
      );
      sessionRepository.save(finishedSession);
    } catch (Exception e) {
      log.error("Encountered exception during import", e);
      FinishedSession finishedSession = importInProgressSession.finishFailed();
      sessionRepository.save(finishedSession);
    }
  }

  public AccountsInfo findSingleImport(SessionId sessionId) {
    return sessionRepository.getFinishedSession(sessionId).accountsInfo();
  }
}
