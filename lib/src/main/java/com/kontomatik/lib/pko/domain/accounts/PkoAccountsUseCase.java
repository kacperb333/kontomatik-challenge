package com.kontomatik.lib.pko.domain.accounts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kontomatik.lib.GsonUtils;
import com.kontomatik.lib.HttpClient;
import com.kontomatik.lib.HttpClient.PostRequest;
import com.kontomatik.lib.HttpClient.Response;
import com.kontomatik.lib.pko.domain.PkoConstants;
import com.kontomatik.lib.pko.domain.signin.LoggedInPkoSession;

import java.util.Map;

public class PkoAccountsUseCase {
  private final HttpClient httpClient;

  public PkoAccountsUseCase(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public Accounts fetchAccounts(LoggedInPkoSession loggedInPkoSession) {
    PostRequest postRequest = prepareAccountsRequest(loggedInPkoSession);
    Response response = httpClient.post("/init", postRequest);
    Map<String, JsonElement> accounts = response.extractMap("response", "data", "accounts");
    return parseAccounts(accounts);
  }

  private static PostRequest prepareAccountsRequest(LoggedInPkoSession loggedInPkoSession) {
    return PostRequest.Builder
      .withStandardHeaders()
      .withHeader(PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(loggedInPkoSession))
      .withBody(AccountsRequest.newRequest())
      .build();
  }

  private static String extractPkoSessionId(LoggedInPkoSession loggedInPkoSession) {
    return loggedInPkoSession.pkoSessionId().value();
  }

  private static Accounts parseAccounts(Map<String, JsonElement> accounts) {
    return new Accounts(
      accounts.values().stream()
        .map(it -> parseAccount(it.getAsJsonObject()))
        .toList()
    );
  }

  //TODO remove dependency on gson?
  private static Account parseAccount(JsonObject jsonAccount) {
    return new Account(
      new Account.Name(GsonUtils.extractString(jsonAccount, "name")),
      new Account.Balance(
        new Account.Balance.Amount(GsonUtils.extractString(jsonAccount, "balance")),
        new Account.Balance.Currency(GsonUtils.extractString(jsonAccount, "currency"))
      )
    );
  }
}
