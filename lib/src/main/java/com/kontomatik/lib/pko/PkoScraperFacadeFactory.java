package com.kontomatik.lib.pko;

import com.kontomatik.lib.ScraperHttpClient;
import com.kontomatik.lib.httpclient.ScraperApacheHttpClient;
import com.kontomatik.lib.pko.domain.accounts.PkoAccountsUseCase;
import com.kontomatik.lib.pko.domain.signin.PkoSignInUseCase;
import org.apache.http.impl.client.HttpClientBuilder;

public class PkoScraperFacadeFactory {

  public static PkoScraperFacade pkoScraperFacade() {
    return pkoScraperFacade("https://www.ipko.pl/ipko3");
  }

  static PkoScraperFacade pkoScraperFacade(String baseUrl) {
    ScraperHttpClient httpClient = new ScraperApacheHttpClient(baseUrl, HttpClientBuilder.create());
    var pkoSignInUseCase = new PkoSignInUseCase(httpClient);
    var pkoAccountsUseCase = new PkoAccountsUseCase(httpClient);
    return new PkoScraperFacade(pkoSignInUseCase, pkoAccountsUseCase);
  }
}
