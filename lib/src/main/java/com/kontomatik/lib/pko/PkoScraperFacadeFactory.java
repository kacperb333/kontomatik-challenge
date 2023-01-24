package com.kontomatik.lib.pko;

import com.kontomatik.lib.HttpClient;
import com.kontomatik.lib.httpclient.ApacheHttpClient;
import com.kontomatik.lib.pko.domain.accounts.PkoAccountsUseCase;
import com.kontomatik.lib.pko.domain.signin.PkoSignInUseCase;
import org.apache.http.impl.client.HttpClientBuilder;

public class PkoScraperFacadeFactory {

  public static PkoScraperFacade pkoScraperFacade() {
    return pkoScraperFacade("https://www.ipko.pl/ipko3");
  }

  static PkoScraperFacade pkoScraperFacade(String baseUrl) {
    HttpClient httpClient = new ApacheHttpClient(baseUrl, HttpClientBuilder.create());
    var pkoSignInUseCase = new PkoSignInUseCase(httpClient);
    var pkoAccountsUseCase = new PkoAccountsUseCase(httpClient);
    return new PkoScraperFacade(pkoSignInUseCase, pkoAccountsUseCase);
  }
}
