package com.kontomatik.lib.pko.domain.accounts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kontomatik.lib.GsonUtils;
import com.kontomatik.lib.ScraperHttpClient;
import com.kontomatik.lib.pko.domain.PkoConstants;
import com.kontomatik.lib.pko.domain.signin.LoggedInPkoSession;

import java.util.Map;

public class PkoAccountsUseCase {
  private final ScraperHttpClient httpClient;

  public PkoAccountsUseCase(ScraperHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public Accounts fetchAccounts(LoggedInPkoSession loggedInPkoSession) {
    ScraperHttpClient.PostRequest postRequest = prepareAccountsRequest(loggedInPkoSession);
    return httpClient.post("/init", postRequest, (responseHeaders, jsonResponse) -> {
      JsonObject response = GsonUtils.parseToObject(jsonResponse);
      Map<String, JsonElement> accounts = GsonUtils.extractMap(response, "response", "data", "accounts");
      return parseAccounts(accounts);
    });
  }

  private static ScraperHttpClient.PostRequest prepareAccountsRequest(LoggedInPkoSession loggedInPkoSession) {
    AccountsRequest accountsRequest = AccountsRequest.newRequest();
    return new ScraperHttpClient.PostRequest(
      Map.of(
        "accept", "application/json",
        PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(loggedInPkoSession)
      ),
      accountsRequest
    );
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
