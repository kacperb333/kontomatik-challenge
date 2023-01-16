package com.kontomatik.pko.lib;

import com.kontomatik.pko.lib.client.PkoClientFactory;
import com.kontomatik.pko.lib.usecase.accounts.PkoAccountInfoUseCase;
import com.kontomatik.pko.lib.usecase.login.PkoLoginUseCase;

public class PkoScraperFacadeFactory {

  public static PkoScraperFacade pkoScraperFacade() {
    return pkoScraperFacade("https://www.ipko.pl/");
  }

  static PkoScraperFacade pkoScraperFacade(String baseUrl) {
    var pkoClient = PkoClientFactory.pkoClient(baseUrl);
    var pkoLoginUseCase = new PkoLoginUseCase(pkoClient);
    var pkoAccountInfoUseCase = new PkoAccountInfoUseCase(pkoClient);
    return new PkoScraperFacade(pkoLoginUseCase, pkoAccountInfoUseCase);
  }
}
