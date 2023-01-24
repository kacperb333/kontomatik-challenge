package com.kontomatik.service.pko

import com.kontomatik.lib.pko.PkoScraperFacade
import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.signin.Credentials
import com.kontomatik.lib.pko.domain.signin.InvalidCredentials
import com.kontomatik.lib.pko.domain.signin.Otp
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
    stubScraperSignIn("test-login", "test-password")
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
    HttpResponseWrapper logInResponse = postSignIn("test-login", "test-password")

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
    stubScraperSignIn("test-login", "test-password")
    stubScraperOtp("test-otp")
    stubScrapperAccounts {
      importLatch.await()
      testAccounts()
    }

    and:
    HttpResponseWrapper signInResponse = postSignIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(signInResponse))

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

  def "should return login failed response with proper information for the user in case sign in fails"() {
    given:
    stubScraperSignIn("test-login", "test-password") { throw new InvalidCredentials() }

    when:
    HttpResponseWrapper signInResponse = postSignIn("test-login", "test-password")

    then:
    signInResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    with(new JsonSlurper().parseText(signInResponse.body)) {
      it.code == "InvalidCredentials"
    }
  }

  def "should return login failed response with proper information for the user in case otp input fails"() {
    given:
    stubScraperSignIn("test-login", "test-password")
    stubScraperOtp("test-otp") { throw new InvalidCredentials() }

    when:
    HttpResponseWrapper signInResponse = postSignIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(signInResponse))

    then:
    otpResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    with(new JsonSlurper().parseText(otpResponse.body)) {
      it.code == "InvalidCredentials"
    }
  }

  def "should return internal sever error on unexpected exception"() {
    given:
    stubScraperSignIn("test-login", "test-password")
    stubScraperOtp("test-otp") { throw new RuntimeException("Something went wrong") }

    when:
    HttpResponseWrapper signInResponse = postSignIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(signInResponse))

    then:
    otpResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    with(new JsonSlurper().parseText(otpResponse.body)) {
      it.code == "Error"
    }
  }

  def "should return info about failed import when accounts import fails for any reason"() {
    given:
    stubScraperSignIn("test-login", "test-password")
    stubScraperOtp("test-otp")
    stubScrapperAccounts { throw new RuntimeException("Something went wrong") }

    when:
    HttpResponseWrapper signInResponse = postSignIn("test-login", "test-password")

    and:
    HttpResponseWrapper otpResponse = postOtp("test-otp", extractSessionId(signInResponse))

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

  private void stubScraperSignIn(
    String login,
    String password,
    Closure logInClosure = { testOtpRequiredPkoSession() }
  ) {
    pkoScraperFacade.signIn(new Credentials(login, password)) >> { logInClosure() }
  }

  private void stubScraperOtp(
    String otp,
    Closure otpClosure = { testLoggedInPkoSession() }
  ) {
    pkoScraperFacade.inputOtp(testOtpRequiredPkoSession(), new Otp(otp)) >> { otpClosure() }
  }

  private void stubScrapperAccounts(
    Closure accountsClosure
  ) {
    pkoScraperFacade.fetchAccounts(testLoggedInPkoSession()) >> { accountsClosure() }
  }

  private HttpResponseWrapper postSignIn(String login, String password) {
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
