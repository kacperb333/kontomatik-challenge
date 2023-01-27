package com.kontomatik.lib.pko

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.kontomatik.lib.pko.domain.PkoConstants
import org.apache.http.HttpHost
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.ssl.SSLContexts
import spock.lang.Specification

import javax.net.ssl.SSLContext

import static com.github.tomakehurst.wiremock.client.WireMock.*

abstract class PkoApiMockBaseSpec extends Specification {

  private WireMockServer wireMock = new WireMockServer(
    WireMockConfiguration.options()
      .enableBrowserProxying(true)
      .port(8090)
  )

  HttpClientBuilder configureApacheHttpTestProxy() {
    TrustStrategy acceptingTrustStrategy = new TrustAllStrategy()
    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(acceptingTrustStrategy).build();
    SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    HttpClientBuilder.create()
      .setProxy(HttpHost.create("http://localhost:8090"))
      .setSSLSocketFactory(connectionSocketFactory)
  }

  def setup() {
    wireMock.start()
    stubPkoLogin(CORRECT_LOGIN, successfulLoginResponse())
    stubPkoLogin(INCORRECT_LOGIN, wrongLoginResponse())
    stubPkoPassword(CORRECT_PASSWORD, successfulPasswordResponse())
    stubPkoPassword(INCORRECT_PASSWORD, wrongPasswordResponse())
    stubPkoOtp(CORRECT_OTP, successfulOtpResponse())
    stubPkoOtp(INCORRECT_OTP, wrongOtpResponse())
    stubPkoAccounts(successfulAccountsResponse())
  }

  def cleanup() {
    wireMock.stop()
  }

  static String CORRECT_LOGIN = "correct-login"
  static String INCORRECT_LOGIN = "incorrect-login"

  static String CORRECT_PASSWORD = "correct-password"
  static String INCORRECT_PASSWORD = "incorrect-password"

  static String CORRECT_OTP = "correct-otp"
  static String INCORRECT_OTP = "incorrect-otp"

  static String loginRequest(String login) {
    return """
      {
        "version": 3,
        "state_id": "login",
        "data": {
          "login": "$login"
        },
        "action": "submit"
      }
    """
  }

  static ResponseDefinitionBuilder successfulLoginResponse() {
    return okJson("""
      {
        "flow_id": "test-flow",
        "token": "test-token",
        "state_id": "password"
      }
    """)
  }

  static wrongLoginResponse() {
    return okJson("""
      {
        "state_id": "login"
      }
    """)
  }

  static String passwordRequest(String password) {
    return """
      {
        "version": 3,
        "state_id": "password",
        "flow_id": "test-flow",
        "token": "test-token",
        "data": {
          "password": "$password"
        },
        "action": "submit"
      }
    """
  }

  static ResponseDefinitionBuilder successfulPasswordResponse() {
    return okJson("""
      {
        "flow_id": "test-flow",
        "token": "test-token",
        "state_id": "one_time_password"
      }
    """)
  }

  static ResponseDefinitionBuilder wrongPasswordResponse() {
    return okJson("""
      {
        "state_id": "login"
      }
    """)
  }

  static String otpRequest(String otp) {
    return """
      {
        "version": 3,
        "state_id": "one_time_password",
        "flow_id": "test-flow",
        "token": "test-token",
        "data": {
          "otp": "$otp"
        },
        "action": "submit"
      }
    """
  }

  static ResponseDefinitionBuilder successfulOtpResponse() {
    return okJson("""
      {
        "state_id": "END",
        "finished": true
      }
    """)
  }

  static ResponseDefinitionBuilder wrongOtpResponse() {
    return okJson("""
      {
        "state_id": "one_time_password"
      }
    """)
  }

  static String accountsRequest() {
    return """
      {
        "version": 3,
        "data": {
          "accounts": {}
        }
      }
    """
  }

  static ResponseDefinitionBuilder successfulAccountsResponse() {
    return okJson("""
      {
        "response": {
          "data": {
            "accounts": {
              "account-1-id": {
                "name": "account-1",
                "balance": "2000.00",
                "currency": "PLN"
              },
              "account-2-id": {
                "name": "account-2",
                "balance": "3000.00",
                "currency": "EUR"
              }
            }
          }
        }
      }
    """)
  }

  void stubPkoLogin(String login, ResponseDefinitionBuilder jsonResponse) {
    wireMock.stubFor(
      post("/ipko3/login")
        .withRequestBody(equalToJson(loginRequest(login)))
        .willReturn(jsonResponse
          .withHeader(PkoConstants.SESSION_HEADER_NAME, "test-pko-session")
        )
    )
  }

  void stubPkoPassword(String password, ResponseDefinitionBuilder jsonResponse) {
    wireMock.stubFor(
      post("/ipko3/login")
        .withHeader(PkoConstants.SESSION_HEADER_NAME, equalTo("test-pko-session"))
        .withRequestBody(equalToJson(passwordRequest(password)))
        .willReturn(jsonResponse
          .withHeader(PkoConstants.SESSION_HEADER_NAME, "test-pko-session")
        )
    )
  }

  void stubPkoOtp(String otp, ResponseDefinitionBuilder jsonResponse) {
    wireMock.stubFor(
      post("/ipko3/login")
        .withHeader(PkoConstants.SESSION_HEADER_NAME, equalTo("test-pko-session"))
        .withRequestBody(equalToJson(otpRequest(otp)))
        .willReturn(jsonResponse
          .withHeader(PkoConstants.SESSION_HEADER_NAME, "test-pko-session")
        )
    )
  }

  void stubPkoAccounts(ResponseDefinitionBuilder jsonResponse) {
    wireMock.stubFor(
      post("/ipko3/init")
        .withHeader(PkoConstants.SESSION_HEADER_NAME, equalTo("test-pko-session"))
        .withRequestBody(equalToJson(accountsRequest()))
        .willReturn(jsonResponse
          .withHeader(PkoConstants.SESSION_HEADER_NAME, "test-pko-session")
        )
    )
  }
}
