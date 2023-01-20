package com.kontomatik.lib.pko;

import com.kontomatik.lib.ScraperHttpClient;
import com.kontomatik.lib.httpclient.ScraperApacheHttpClient;
import com.kontomatik.lib.pko.domain.accounts.PkoAccountInfoUseCase;
import com.kontomatik.lib.pko.domain.login.PkoLoginUseCase;
import org.apache.http.impl.client.HttpClientBuilder;

public class PkoScraperFacadeFactory {

  public static PkoScraperFacade pkoScraperFacade() {
    return pkoScraperFacade("https://www.ipko.pl/ipko3");
  }

  static PkoScraperFacade pkoScraperFacade(String baseUrl) {
    ScraperHttpClient httpClient = new ScraperApacheHttpClient(baseUrl, HttpClientBuilder.create());
    var pkoLoginUseCase = new PkoLoginUseCase(httpClient);
    var pkoAccountInfoUseCase = new PkoAccountInfoUseCase(httpClient);
    return new PkoScraperFacade(pkoLoginUseCase, pkoAccountInfoUseCase);
  }
}
