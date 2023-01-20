package com.kontomatik.lib.pko.domain.accounts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kontomatik.lib.GsonUtils;
import com.kontomatik.lib.ScraperHttpClient;
import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.domain.PkoConstants;
import com.kontomatik.lib.pko.domain.login.LoggedInPkoSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

public class PkoAccountInfoUseCase {
  private final ScraperHttpClient httpClient;

  public PkoAccountInfoUseCase(ScraperHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public AccountsInfo fetchAccountInfo(LoggedInPkoSession loggedInPkoSession) {
    return handleExceptions(() ->
      fetchAccounts(loggedInPkoSession)
    );
  }

  private AccountsInfo fetchAccounts(LoggedInPkoSession loggedInPkoSession) throws IOException {
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

  private static AccountsInfo parseAccounts(Map<String, JsonElement> accounts) {
    return new AccountsInfo(
      accounts.values().stream()
        .map(it -> parseAccount(it.getAsJsonObject()))
        .toList()
    );
  }

  private static AccountInfo parseAccount(JsonObject jsonAccount) {
    return new AccountInfo(
      GsonUtils.extractString(jsonAccount, "name"),
      GsonUtils.extractString(jsonAccount, "balance"),
      GsonUtils.extractString(jsonAccount, "currency")
    );
  }

  private static <T> T handleExceptions(Callable<T> toRun) {
    try {
      return toRun.call();
    } catch (Exception e) {
      throw new PkoScraperFacade.PkoScraperFacadeBug(e);
    }
  }
}
