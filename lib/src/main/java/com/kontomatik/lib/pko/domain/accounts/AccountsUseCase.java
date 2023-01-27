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

public class AccountsUseCase {
  private final HttpClient httpClient;

  public AccountsUseCase(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public Accounts fetchAccounts(LoggedInPkoSession loggedInPkoSession) {
    PostRequest postRequest = prepareAccountsRequest(loggedInPkoSession);
    Response response = httpClient.execute(postRequest);
    Map<String, JsonElement> accounts = response.extractMap("response", "data", "accounts");
    return parseAccounts(accounts);
  }

  private static PostRequest prepareAccountsRequest(LoggedInPkoSession loggedInPkoSession) {
    return PostRequest.Builder
      .jsonRequest()
      .withUrl("/init")
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

  private static Account parseAccount(JsonObject jsonAccount) {
    return Account.from(
      GsonUtils.extractString(jsonAccount, "name"),
      GsonUtils.extractString(jsonAccount, "balance"),
      GsonUtils.extractString(jsonAccount, "currency")
    );
  }
}
