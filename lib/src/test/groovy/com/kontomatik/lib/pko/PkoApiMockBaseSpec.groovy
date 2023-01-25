package com.kontomatik.lib.pko

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.kontomatik.lib.pko.domain.PkoConstants
import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static groovy.json.JsonOutput.toJson

abstract class PkoApiMockBaseSpec extends Specification {

  private WireMockServer wireMock = new WireMockServer(8090)

  def setup() {
    wireMock.start()
    stubPkoLogin(CORRECT_LOGIN, successfulLoginResponse())
    stubPkoLogin(INCORRECT_LOGIN, wrongLoginResponse())
    stubPkoPassword(CORRECT_PASSWORD, successfulPasswordResponse())
    stubPkoPassword(INCORRECT_PASSWORD, wrongPasswordResponse())
    stubPkoOtp(CORRECT_OTP, successfulOtpResponse())
    stubPkoOtp(INCORRECT_OTP, wrongOtpResponse())
    stubPkoAccounts(successfulAccountsResponse(CORRECT_RETURNED_ACCOUNTS))

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

  static Accounts CORRECT_RETURNED_ACCOUNTS = new Accounts([
    new Account(
      new Account.Name("account-1"),
      new Account.Balance(
        new Account.Balance.Amount("2000.00"),
        new Account.Balance.Currency("PLN")
      )
    ),
    new Account(
      new Account.Name("account-2"),
      new Account.Balance(
        new Account.Balance.Amount("3000.00"),
        new Account.Balance.Currency("EUR")
      )
    )
  ])

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

  static ResponseDefinitionBuilder successfulAccountsResponse(Accounts accounts) {
    Map accountsMap = accounts.list().collectEntries {
      String accountId = "${it.name().value()}-id"
      [(accountId): [
        "balance" : it.balance().amount().value(),
        "currency": it.balance().currency().value(),
        "name"    : it.name().value()
      ]]
    }

    return okJson("""
      {
        "response": {
          "data": {
            "accounts": ${toJson(accountsMap)}
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
