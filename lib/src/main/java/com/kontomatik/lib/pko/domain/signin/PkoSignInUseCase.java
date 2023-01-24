package com.kontomatik.lib.pko.domain.signin;

import com.kontomatik.lib.HttpClient;
import com.kontomatik.lib.HttpClient.PostRequest;
import com.kontomatik.lib.HttpClient.Response;
import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.domain.PkoConstants;

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
    PostRequest postRequest = prepareLoginRequest(login);
    Response response = httpClient.post("/login", postRequest);
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

  private static PostRequest prepareLoginRequest(String login) {
    return PostRequest.Builder
      .withStandardHeaders()
      .withBody(PkoSignInRequest.newRequest(login))
      .build();
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
    PostRequest postRequest = preparePasswordRequest(password, passwordRequiredPkoSession);
    Response response = httpClient.post("/login", postRequest);
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

  private static PostRequest preparePasswordRequest(String password, PasswordRequiredPkoSession passwordRequiredPkoSession) {
    return PostRequest.Builder
      .withStandardHeaders()
      .withHeader(PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(passwordRequiredPkoSession))
      .withBody(PkoPasswordRequest.newRequest(password, passwordRequiredPkoSession))
      .build();
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
    PostRequest postRequest = prepareOtpRequest(otp, loginInProgressPkoSession);
    Response response = httpClient.post("/login", postRequest);
    String responseState = response.extractString("state_id");
    validateOtpResponseState(responseState);
    String responseSessionId = response.getHeader(PkoConstants.SESSION_HEADER_NAME);
    Objects.requireNonNull(responseSessionId);
    return new LoggedInPkoSession(
      new PkoSessionId(responseSessionId)
    );
  }

  private static PostRequest prepareOtpRequest(String otp, OtpRequiredPkoSession loginInProgressPkoSession) {
    return PostRequest.Builder
      .withStandardHeaders()
      .withHeader(PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(loginInProgressPkoSession))
      .withBody(PkoOtpRequest.newRequest(otp, loginInProgressPkoSession))
      .build();
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
