package com.kontomatik.lib.pko;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.accounts.PkoAccountsUseCase;
import com.kontomatik.lib.pko.domain.login.*;

public class PkoScraperFacade {

  private final PkoLoginUseCase pkoLoginUseCase;
  private final PkoAccountsUseCase pkoAccountsUseCase;

  PkoScraperFacade(PkoLoginUseCase pkoLoginUseCase, PkoAccountsUseCase pkoAccountsUseCase) {
    this.pkoLoginUseCase = pkoLoginUseCase;
    this.pkoAccountsUseCase = pkoAccountsUseCase;
  }

  public LoginInProgressPkoSession logIn(Credentials credentials) {
    return pkoLoginUseCase.logIn(credentials);
  }

  public LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressSession, Otp otp) {
    return pkoLoginUseCase.inputOtp(inProgressSession, otp);
  }

  public Accounts fetchAccounts(LoggedInPkoSession loggedInSession) {
    return pkoAccountsUseCase.fetchAccounts(loggedInSession);
  }

  public static class LoginFailed extends RuntimeException {
    public LoginFailed(String message) {
      super(message);
    }
  }

  public static class PkoScraperFacadeBug extends RuntimeException {
    public PkoScraperFacadeBug(Throwable cause) {
      super(cause);
    }
  }
}

