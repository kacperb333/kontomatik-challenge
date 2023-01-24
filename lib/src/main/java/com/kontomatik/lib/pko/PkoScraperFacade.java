package com.kontomatik.lib.pko;

import com.kontomatik.lib.HttpClient;
import com.kontomatik.lib.httpclient.ApacheHttpClient;
import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.accounts.PkoAccountsUseCase;
import com.kontomatik.lib.pko.domain.signin.*;
import org.apache.http.impl.client.HttpClientBuilder;

public class PkoScraperFacade {

  private final PkoSignInUseCase pkoSignInUseCase;
  private final PkoAccountsUseCase pkoAccountsUseCase;

  private PkoScraperFacade(PkoSignInUseCase pkoSignInUseCase, PkoAccountsUseCase pkoAccountsUseCase) {
    this.pkoSignInUseCase = pkoSignInUseCase;
    this.pkoAccountsUseCase = pkoAccountsUseCase;
  }

  public static PkoScraperFacade pkoScraperFacade() {
    return pkoScraperFacade("https://www.ipko.pl/ipko3");
  }

  static PkoScraperFacade pkoScraperFacade(String baseUrl) {
    HttpClient httpClient = new ApacheHttpClient(baseUrl, HttpClientBuilder.create());
    var pkoSignInUseCase = new PkoSignInUseCase(httpClient);
    var pkoAccountsUseCase = new PkoAccountsUseCase(httpClient);
    return new PkoScraperFacade(pkoSignInUseCase, pkoAccountsUseCase);
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

