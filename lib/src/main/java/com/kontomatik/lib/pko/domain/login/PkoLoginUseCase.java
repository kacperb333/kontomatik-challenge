package com.kontomatik.lib.pko.domain.login;

import com.google.gson.JsonObject;
import com.kontomatik.lib.GsonUtils;
import com.kontomatik.lib.ScraperHttpClient;
import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.domain.PkoConstants;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class PkoLoginUseCase {
  private final ScraperHttpClient httpClient;

  public PkoLoginUseCase(ScraperHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public LoginInProgressPkoSession logIn(Credentials credentials) {
    return handleExceptions(() -> {
      LoginInProgressPkoSession afterLoginSession = enterLogin(credentials.login());
      return enterPassword(credentials.password(), afterLoginSession);
    });
  }

  private LoginInProgressPkoSession enterLogin(String login) throws IOException {
    ScraperHttpClient.PostRequest postRequest = prepareLoginRequest(login);
    return httpClient.post("/login", postRequest, (responseHeaders, jsonResponse) -> {
      JsonObject response = GsonUtils.parseToObject(jsonResponse);
      String responseState = GsonUtils.extractString(response, "state_id");
      validateLoginResponseState(responseState);
      String responseSessionId = responseHeaders.get(PkoConstants.SESSION_HEADER_NAME);
      Objects.requireNonNull(responseSessionId);
      String responseToken = GsonUtils.extractString(response, "token");
      String responseFlowId = GsonUtils.extractString(response, "flow_id");
      return new LoginInProgressPkoSession(
        new PkoSessionId(responseSessionId),
        new FlowId(responseFlowId),
        new Token(responseToken)
      );
    });
  }

  private static ScraperHttpClient.PostRequest prepareLoginRequest(String login) {
    PkoLoginRequest loginRequest = PkoLoginRequest.newRequest(login);
    return new ScraperHttpClient.PostRequest(
      Map.of(
        "accept", "application/json"
      ),
      loginRequest
    );
  }

  private static void validateLoginResponseState(String responseStateId) {
    if (Objects.equals(responseStateId, "login")) {
      throw new PkoScraperFacade.LoginFailed("Invalid credentials");
    }
    if (!Objects.equals(responseStateId, "password")) {
      throw new IllegalStateException("Wrong state id for username response");
    }
  }

  private LoginInProgressPkoSession enterPassword(String password, LoginInProgressPkoSession loginInProgressPkoSession) throws IOException {
    ScraperHttpClient.PostRequest postRequest = preparePasswordRequest(password, loginInProgressPkoSession);
    return httpClient.post("/login", postRequest, (responseHeaders, jsonResponse) -> {
      JsonObject response = GsonUtils.parseToObject(jsonResponse);
      String responseState = GsonUtils.extractString(response, "state_id");
      validatePasswordResponseState(responseState);
      String responseSessionId = responseHeaders.get(PkoConstants.SESSION_HEADER_NAME);
      Objects.requireNonNull(responseSessionId);
      String responseToken = GsonUtils.extractString(response, "token");
      String responseFlowId = GsonUtils.extractString(response, "flow_id");
      return new LoginInProgressPkoSession(
        new PkoSessionId(responseSessionId),
        new FlowId(responseFlowId),
        new Token(responseToken)
      );
    });
  }

  private static ScraperHttpClient.PostRequest preparePasswordRequest(String password, LoginInProgressPkoSession loginInProgressPkoSession) {
    PkoPasswordRequest passwordRequest = PkoPasswordRequest.newRequest(password, loginInProgressPkoSession);
    return new ScraperHttpClient.PostRequest(
      Map.of(
        "accept", "application/json",
        PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(loginInProgressPkoSession)
      ),
      passwordRequest
    );
  }

  private static void validatePasswordResponseState(String responseStateId) {
    if (Objects.equals(responseStateId, "login")) {
      throw new PkoScraperFacade.LoginFailed("Invalid credentials");
    }
    if (!Objects.equals(responseStateId, "one_time_password")) {
      throw new IllegalStateException("Wrong state id for password response");
    }
  }


  public LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressLoginSession, Otp otp) {
    return handleExceptions(() ->
      enterOtp(otp.code(), inProgressLoginSession)
    );
  }

  private LoggedInPkoSession enterOtp(String otp, LoginInProgressPkoSession loginInProgressPkoSession) throws IOException {
    ScraperHttpClient.PostRequest postRequest = prepareOtpRequest(otp, loginInProgressPkoSession);
    return httpClient.post("/login", postRequest, (responseHeaders, jsonResponse) -> {
      JsonObject response = GsonUtils.parseToObject(jsonResponse);
      String responseState = GsonUtils.extractString(response, "state_id");
      validateOtpResponseState(responseState);
      String responseSessionId = responseHeaders.get(PkoConstants.SESSION_HEADER_NAME);
      Objects.requireNonNull(responseSessionId);
      return new LoggedInPkoSession(
        new PkoSessionId(responseSessionId)
      );
    });
  }

  private static ScraperHttpClient.PostRequest prepareOtpRequest(String otp, LoginInProgressPkoSession loginInProgressPkoSession) {
    PkoOtpRequest otpRequest = PkoOtpRequest.newRequest(otp, loginInProgressPkoSession);
    return new ScraperHttpClient.PostRequest(
      Map.of(
        "accept", "application/json",
        PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(loginInProgressPkoSession)
      ),
      otpRequest
    );
  }

  private static void validateOtpResponseState(String responseStateId) {
    if (Objects.equals(responseStateId, "one_time_password")) {
      throw new PkoScraperFacade.LoginFailed("Wrong one time password");
    }

    if (!Objects.equals(responseStateId, "END")) {
      throw new IllegalStateException("Wrong state id for otp response");
    }
  }

  private static String extractPkoSessionId(LoginInProgressPkoSession loginInProgressPkoSession) {
    return loginInProgressPkoSession.pkoSessionId().value();
  }

  private static <T> T handleExceptions(Callable<T> toRun) {
    try {
      return toRun.call();
    } catch (PkoScraperFacade.LoginFailed e) {
      throw e;
    } catch (Exception e) {
      throw new PkoScraperFacade.PkoScraperFacadeBug(e);
    }
  }
}
