package com.kontomatik.pko.lib.usecase.accounts;

import com.kontomatik.pko.lib.client.PkoClient;
import com.kontomatik.pko.lib.usecase.login.LoggedInPkoSession;

public class PkoAccountInfoUseCase {
  private final PkoClient pkoClient;

  public PkoAccountInfoUseCase(PkoClient pkoClient) {
    this.pkoClient = pkoClient;
  }

  public AccountsInfo fetchAccountInfo(LoggedInPkoSession session) {
    return pkoClient.fetchAccounts(new PkoClient.PkoFetchAccountsInput(session.pkoSessionId()));
  }
}
