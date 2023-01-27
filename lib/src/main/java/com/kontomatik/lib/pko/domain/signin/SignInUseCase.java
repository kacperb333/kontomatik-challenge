package com.kontomatik.lib.pko.domain.signin;

import com.kontomatik.lib.HttpClient;
import com.kontomatik.lib.HttpClient.PostRequest;
import com.kontomatik.lib.HttpClient.Response;
import com.kontomatik.lib.pko.domain.PkoConstants;

import java.util.Objects;

public class SignInUseCase {
  private final HttpClient httpClient;

  public SignInUseCase(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public OtpRequiredPkoSession signIn(Credentials credentials) {
    PasswordRequiredPkoSession afterLoginSession = enterLogin(credentials.login());
    return enterPassword(credentials.password(), afterLoginSession);
  }

  private PasswordRequiredPkoSession enterLogin(String login) {
    PostRequest postRequest = prepareLoginRequest(login);
    Response response = httpClient.post("/login", postRequest);
    assertCorrectLogin(response);
    assertPasswordRequired(response);
    String responseSessionId = assertContainsSessionId(response);
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
      .jsonRequest()
      .withBody(PkoSignInRequest.newRequest(login))
      .build();
  }

  private static void assertCorrectLogin(Response response) {
    if (Objects.equals(extractResponseStateId(response), "login")) {
      throw new InvalidCredentials();
    }
  }

  private static void assertPasswordRequired(Response response) {
    if (!Objects.equals(extractResponseStateId(response), "password")) {
      throw new IllegalStateException("Wrong state id for username response");
    }
  }

  private OtpRequiredPkoSession enterPassword(String password, PasswordRequiredPkoSession passwordRequiredPkoSession) {
    PostRequest postRequest = preparePasswordRequest(password, passwordRequiredPkoSession);
    Response response = httpClient.post("/login", postRequest);
    assertCorrectPassword(response);
    assertOneTimePasswordRequired(response);
    String responseSessionId = assertContainsSessionId(response);
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
      .jsonRequest()
      .withHeader(PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(passwordRequiredPkoSession))
      .withBody(PkoPasswordRequest.newRequest(password, passwordRequiredPkoSession))
      .build();
  }

  private static void assertCorrectPassword(Response response) {
    if (Objects.equals(extractResponseStateId(response), "login")) {
      throw new InvalidCredentials();
    }
  }

  private static void assertOneTimePasswordRequired(Response response) {
    if (!Objects.equals(extractResponseStateId(response), "one_time_password")) {
      throw new IllegalStateException("Wrong state id for password response");
    }
  }

  public LoggedInPkoSession inputOtp(OtpRequiredPkoSession otpRequiredSession, Otp otp) {
    return enterOtp(otp.code(), otpRequiredSession);
  }

  private LoggedInPkoSession enterOtp(String otp, OtpRequiredPkoSession loginInProgressPkoSession) {
    PostRequest postRequest = prepareOtpRequest(otp, loginInProgressPkoSession);
    Response response = httpClient.post("/login", postRequest);
    assertCorrectOtp(response);
    assertSignInFinished(response);
    String responseSessionId = assertContainsSessionId(response);
    return new LoggedInPkoSession(
      new PkoSessionId(responseSessionId)
    );
  }

  private static PostRequest prepareOtpRequest(String otp, OtpRequiredPkoSession loginInProgressPkoSession) {
    return PostRequest.Builder
      .jsonRequest()
      .withHeader(PkoConstants.SESSION_HEADER_NAME, extractPkoSessionId(loginInProgressPkoSession))
      .withBody(PkoOtpRequest.newRequest(otp, loginInProgressPkoSession))
      .build();
  }

  private static void assertCorrectOtp(Response response) {
    if (Objects.equals(extractResponseStateId(response), "one_time_password")) {
      throw new InvalidCredentials();
    }
  }

  private static void assertSignInFinished(Response response) {
    if (!Objects.equals(extractResponseStateId(response), "END")) {
      throw new IllegalStateException("Wrong state id for otp response");
    }
  }

  private static String extractResponseStateId(Response response) {
    return response.extractString("state_id");
  }

  private static String assertContainsSessionId(Response response) {
    String responseSessionId = response.getHeader(PkoConstants.SESSION_HEADER_NAME);
    Objects.requireNonNull(responseSessionId);
    return responseSessionId;
  }

  private static String extractPkoSessionId(PasswordRequiredPkoSession passwordRequiredPkoSession) {
    return passwordRequiredPkoSession.pkoSessionId().value();
  }

  private static String extractPkoSessionId(OtpRequiredPkoSession otpRequiredPkoSession) {
    return otpRequiredPkoSession.pkoSessionId().value();
  }
}
