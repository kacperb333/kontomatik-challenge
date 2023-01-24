package com.kontomatik.service.pko

import com.kontomatik.lib.pko.PkoScraperFacade
import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.login.Credentials
import com.kontomatik.lib.pko.domain.login.Otp
import com.kontomatik.service.HttpResponseWrapper
import com.kontomatik.service.IntegrationSpec
import groovy.json.JsonSlurper
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpStatus
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CountDownLatch

import static com.kontomatik.service.pko.PkoLibFactories.*
import static com.kontomatik.service.pko.PkoScraperController.SESSION_HEADER

class PkoScraperApiIntSpec extends IntegrationSpec {

  @SpringBean
  PkoScraperFacade pkoScraperFacade = Stub()
  PollingConditions poll = new PollingConditions(timeout: 1)

  def "should log in and fetch accounts info on successful scraper facade invocations"() {
    given:
    stubScraperLogIn("test-login", "test-password")
    stubScraperOtp("test-otp")
    stubScrapperAccounts {
      new Accounts([
        new Account(
          new Account.Name("account-1"),
          new Account.Balance(
            new Account.Balance.Amount("1000.00"),
            new Account.Balance.Currency("PLN")
          )
        ),
        new Account(
          new Account.Name("account-2"),
          new Account.Balance(
            new Account.Balance.Amount("2000.00"),
            new Account.Balance.Currency("EUR")
          )
        ),
        new Account(
          new Account.Name("account-3"),
          new Account.Balance(
            new Account.Balance.Amount("3000.00"),
            new Account.Balance.Currency("USD")
          )
        )
      ])
    }

    when:
    HttpResponseWrapper logInResponse = postLogIn("test-login", "test-password")

    then:
    logInResponse.statusCode == HttpStatus.OK

    when:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(logInResponse))

    then:
    otpResponse.statusCode == HttpStatus.OK

    and:
    poll.eventually {
      HttpResponseWrapper accountsResponse = getAccounts(extractSessionId(otpResponse))
      accountsResponse.statusCode == HttpStatus.OK
      with(new JsonSlurper().parseText(accountsResponse.body)) {
        !it.isFailed
        it.accounts.size() == 3
        with(it.accounts.find { it.name == "account-1" }) {
          it.balance == "1000.00"
          it.currency == "PLN"
        }
        with(it.accounts.find { it.name == "account-2" }) {
          it.balance == "2000.00"
          it.currency == "EUR"
        }
        with(it.accounts.find { it.name == "account-3" }) {
          it.balance == "3000.00"
          it.currency == "USD"
        }
      }
    }
  }

  def "should return empty accounts response until accounts fetch finishes successfully"() {
    given:
    CountDownLatch importLatch = new CountDownLatch(1)

    and:
    stubScraperLogIn("test-login", "test-password")
    stubScraperOtp("test-otp")
    stubScrapperAccounts {
      importLatch.await()
      testAccounts()
    }

    and:
    HttpResponseWrapper logInResponse = postLogIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(logInResponse))

    when:
    HttpResponseWrapper emptyAccountsResponse = getAccounts(extractSessionId(otpResponse))

    then:
    emptyAccountsResponse.statusCode == HttpStatus.OK
    with(new JsonSlurper().parseText(emptyAccountsResponse.body)) {
      it.accounts.isEmpty()
      !it.isFailed
    }

    when:
    importLatch.countDown()

    then:
    poll.eventually {
      HttpResponseWrapper accountsResponse = getAccounts(extractSessionId(otpResponse))
      accountsResponse.statusCode == HttpStatus.OK
      with(new JsonSlurper().parseText(accountsResponse.body)) {
        !it.accounts.isEmpty()
        !it.isFailed
      }
    }
  }

  def "should return login failed response with proper information for the user in case login fails"() {
    given:
    String messageForUser = "Some important login and password message for the user to see"
    stubScraperLogIn("test-login", "test-password") { throw new PkoScraperFacade.LoginFailed(messageForUser) }

    when:
    HttpResponseWrapper loginResponse = postLogIn("test-login", "test-password")

    then:
    loginResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    with(new JsonSlurper().parseText(loginResponse.body)) {
      it.code == "LoginFailed"
      it.message.contains(messageForUser)
    }
  }

  def "should return login failed response with proper information for the user in case otp input fails"() {
    given:
    String messageForUser = "Some important otp message for the user to see"
    stubScraperLogIn("test-login", "test-password")
    stubScraperOtp("test-otp") { throw new PkoScraperFacade.LoginFailed(messageForUser) }

    when:
    HttpResponseWrapper loginResponse = postLogIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(loginResponse))

    then:
    otpResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    with(new JsonSlurper().parseText(otpResponse.body)) {
      it.code == "LoginFailed"
      it.message.contains(messageForUser)
    }
  }

  def "should return internal sever error on unexpected exception"() {
    given:
    stubScraperLogIn("test-login", "test-password")
    stubScraperOtp("test-otp") { throw new PkoScraperFacade.PkoScraperFacadeBug(new RuntimeException("Something went wrong")) }

    when:
    HttpResponseWrapper loginResponse = postLogIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(loginResponse))

    then:
    otpResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    with(new JsonSlurper().parseText(otpResponse.body)) {
      it.code == "PkoScraperFacadeBug"
    }
  }

  def "should return info about failed import when accounts import fails for any reason"() {
    given:
    stubScraperLogIn("test-login", "test-password")
    stubScraperOtp("test-otp")
    stubScrapperAccounts { throw new RuntimeException("Something went wrong") }

    when:
    HttpResponseWrapper loginResponse = postLogIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(loginResponse))

    then:
    poll.eventually {
      HttpResponseWrapper accountsResponse = getAccounts(extractSessionId(otpResponse))
      accountsResponse.statusCode == HttpStatus.OK
      with(new JsonSlurper().parseText(accountsResponse.body)) {
        it.accounts.isEmpty()
        it.isFailed
      }
    }
  }

  private void stubScraperLogIn(
    String login,
    String password,
    Closure logInClosure = { testLoginInProgressPkoSession() }
  ) {
    pkoScraperFacade.logIn(new Credentials(login, password)) >> { logInClosure() }
  }

  private void stubScraperOtp(
    String otp,
    Closure otpClosure = { testLoggedInPkoSession() }
  ) {
    pkoScraperFacade.inputOtp(testLoginInProgressPkoSession(), new Otp(otp)) >> { otpClosure() }
  }

  private void stubScrapperAccounts(
    Closure accountsClosure
  ) {
    pkoScraperFacade.fetchAccounts(testLoggedInPkoSession()) >> { accountsClosure() }
  }

  private HttpResponseWrapper postLogIn(String login, String password) {
    return post(
      [
        "content-type": "application/json"
      ],
      "/session",
      """
        {
          "credentials": {
            "login": "$login",
            "password": "$password"
          }
        }
      """
    )
  }

  private HttpResponseWrapper postOtp(String otp, String sessionId) {
    return post(
      [
        "content-type"  : "application/json",
        (SESSION_HEADER): sessionId
      ],
      "/session/otp",
      """
        {
          "otp": {
            "code": "$otp"
          }
        }
      """
    )
  }

  private HttpResponseWrapper getAccounts(String sessionId) {
    get(
      [
        (SESSION_HEADER): sessionId
      ],
      "/session/accounts"
    )
  }

  private String extractSessionId(HttpResponseWrapper response) {
    return response.headers.getFirst(SESSION_HEADER)
  }
}
