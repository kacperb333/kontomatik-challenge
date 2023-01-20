package com.kontomatik.lib.pko;

import com.kontomatik.lib.pko.domain.accounts.AccountsInfo;
import com.kontomatik.lib.pko.domain.accounts.PkoAccountInfoUseCase;
import com.kontomatik.lib.pko.domain.login.*;

public class PkoScraperFacade {

  private final PkoLoginUseCase pkoLoginUseCase;
  private final PkoAccountInfoUseCase pkoAccountInfoUseCase;

  PkoScraperFacade(PkoLoginUseCase pkoLoginUseCase, PkoAccountInfoUseCase pkoAccountInfoUseCase) {
    this.pkoLoginUseCase = pkoLoginUseCase;
    this.pkoAccountInfoUseCase = pkoAccountInfoUseCase;
  }

  public LoginInProgressPkoSession logIn(Credentials credentials) {
    return pkoLoginUseCase.logIn(credentials);
  }

  public LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressSession, Otp otp) {
    return pkoLoginUseCase.inputOtp(inProgressSession, otp);
  }

  public AccountsInfo fetchAccountsInfo(LoggedInPkoSession loggedInSession) {
    return pkoAccountInfoUseCase.fetchAccountInfo(loggedInSession);
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

