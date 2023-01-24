package com.kontomatik.lib.pko;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.accounts.PkoAccountsUseCase;
import com.kontomatik.lib.pko.domain.signin.*;

public class PkoScraperFacade {

  private final PkoSignInUseCase pkoSignInUseCase;
  private final PkoAccountsUseCase pkoAccountsUseCase;

  PkoScraperFacade(PkoSignInUseCase pkoSignInUseCase, PkoAccountsUseCase pkoAccountsUseCase) {
    this.pkoSignInUseCase = pkoSignInUseCase;
    this.pkoAccountsUseCase = pkoAccountsUseCase;
  }

  public OtpRequiredPkoSession signIn(Credentials credentials) {
    return pkoSignInUseCase.signIn(credentials);
  }

  public LoggedInPkoSession inputOtp(OtpRequiredPkoSession otpRequiredPkoSession, Otp otp) {
    return pkoSignInUseCase.inputOtp(otpRequiredPkoSession, otp);
  }

  public Accounts fetchAccounts(LoggedInPkoSession loggedInSession) {
    return pkoAccountsUseCase.fetchAccounts(loggedInSession);
  }

  public static class LoginFailed extends RuntimeException {
    public LoginFailed(String message) {
      super(message);
    }
  }
}

