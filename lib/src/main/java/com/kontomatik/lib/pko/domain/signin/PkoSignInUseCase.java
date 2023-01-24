package com.kontomatik.lib.pko.domain.signin;

import com.kontomatik.lib.HttpClient;
import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.domain.PkoConstants;

import java.util.Map;
import java.util.Objects;

public class PkoSignInUseCase {
  private final HttpClient httpClient;

  public PkoSignInUseCase(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public OtpRequiredPkoSession signIn(Credentials credentials) {
    PasswordRequiredPkoSession afterLoginSession = enterLogin(credentials.login());
    return enterPassword(credentials.password(), afterLoginSession);
  }

  private PasswordRequiredPkoSession enterLogin(String login) {
    HttpClient.PostRequest postRequest = prepareLoginRequest(login);
    HttpClient.Response response = httpClient.post("/login", postRequest);
    String responseState = response.extractString("state_id");
    validateLoginResponseState(responseState);
    String responseSessionId = response.getHeader(PkoConstants.SESSION_HEADER_NAME);
    Objects.requireNonNull(responseSessionId);
    String responseToken = response.extractString("token");
    String responseFlowId = response.extractString("flow_id");
    return new PasswordRequiredPkoSession(
      new PkoSessionId(responseSessionId),
      new FlowId(responseFlowId),
      new Token(responseToken)
    );
  }

  private static HttpClient.PostRequest prepareLoginRequest(String login) {
    PkoSignInRequest loginRequest = PkoSignInRequest.newRequest(login);
    return new HttpClient.PostRequest(
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

  private OtpRequiredPkoSession enterPassword(String password, PasswordRequiredPkoSession passwordRequiredPkoSession) {
    HttpClient.PostRequest postRequest = preparePasswordRequest(password, passwordRequiredPkoSession);
    HttpClient.Response response = httpClient.post("/login", postRequest);
    String responseState = response.extractString("state_id");
    validatePasswordResponseState(responseState);
    String responseSessionId = response.getHeader(PkoConstants.SESSION_HEADER_NAME);
    Objects.requireNonNull(responseSessionId);
    String responseToken = response.extractString("token");
    String responseFlowId = response.extractString("flow_id");
    return new OtpRequiredPkoSession(
      new PkoSessionId(responseSessionId),
      new FlowId(responseFlowId),
      new Token(responseToken)
    );
  }

  private static HttpClient.PostRequest preparePasswordRequest(String password, PasswordRequiredPkoSession passwordRequiredPkoSession) {
    PkoPasswordRequest passwordRequest = PkoPasswordRequest.newRequest(password, passwordRequiredPkoSession);
    return new HttpClient.PostRequest(
      Map.of(
        "accept", "application/json",
        PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(passwordRequiredPkoSession)
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


  public LoggedInPkoSession inputOtp(OtpRequiredPkoSession otpRequiredSession, Otp otp) {
    return enterOtp(otp.code(), otpRequiredSession);
  }

  private LoggedInPkoSession enterOtp(String otp, OtpRequiredPkoSession loginInProgressPkoSession) {
    HttpClient.PostRequest postRequest = prepareOtpRequest(otp, loginInProgressPkoSession);
    HttpClient.Response response = httpClient.post("/login", postRequest);
    String responseState = response.extractString("state_id");
    validateOtpResponseState(responseState);
    String responseSessionId = response.getHeader(PkoConstants.SESSION_HEADER_NAME);
    Objects.requireNonNull(responseSessionId);
    return new LoggedInPkoSession(
      new PkoSessionId(responseSessionId)
    );
  }

  private static HttpClient.PostRequest prepareOtpRequest(String otp, OtpRequiredPkoSession loginInProgressPkoSession) {
    PkoOtpRequest otpRequest = PkoOtpRequest.newRequest(otp, loginInProgressPkoSession);
    return new HttpClient.PostRequest(
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

  private static String extractPkoSessionId(PasswordRequiredPkoSession passwordRequiredPkoSession) {
    return passwordRequiredPkoSession.pkoSessionId().value();
  }

  private static String extractPkoSessionId(OtpRequiredPkoSession otpRequiredPkoSession) {
    return otpRequiredPkoSession.pkoSessionId().value();
  }
}
