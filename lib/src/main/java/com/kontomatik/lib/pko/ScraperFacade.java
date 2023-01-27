package com.kontomatik.lib.pko;

import com.kontomatik.lib.HttpClient;
import com.kontomatik.lib.httpclient.ApacheHttpClient;
import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.accounts.AccountsUseCase;
import com.kontomatik.lib.pko.domain.signin.*;
import org.apache.http.impl.client.HttpClientBuilder;

public class ScraperFacade {

  private final SignInUseCase pkoSignInUseCase;
  private final AccountsUseCase pkoAccountsUseCase;

  public static ScraperFacade scraperFacade() {
    return scraperFacade(HttpClientBuilder.create());
  }

  static ScraperFacade scraperFacade(HttpClientBuilder httpClientBuilder) {
    HttpClient httpClient = new ApacheHttpClient(httpClientBuilder);
    var pkoSignInUseCase = new SignInUseCase(httpClient);
    var pkoAccountsUseCase = new AccountsUseCase(httpClient);
    return new ScraperFacade(pkoSignInUseCase, pkoAccountsUseCase);
  }

  private ScraperFacade(SignInUseCase pkoSignInUseCase, AccountsUseCase pkoAccountsUseCase) {
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
}

