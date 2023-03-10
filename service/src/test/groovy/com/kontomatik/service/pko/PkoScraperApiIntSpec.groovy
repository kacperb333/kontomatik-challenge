package com.kontomatik.service.pko

import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.service.pko.utils.HttpResponseWrapper
import com.kontomatik.service.pko.utils.ServiceClient
import org.springframework.http.HttpStatus
import spock.util.concurrent.PollingConditions

import java.time.Instant
import java.util.concurrent.CountDownLatch

import static java.time.temporal.ChronoUnit.HOURS
import static java.time.temporal.ChronoUnit.MINUTES

class PkoScraperApiIntSpec extends ScraperFacadeMockBaseSpec {

  PollingConditions poll = new PollingConditions(timeout: 1)
  PollingConditions retentionPoll = new PollingConditions(timeout: 70)
  ServiceClient serviceClient

  def setup() {
    serviceClient = new ServiceClient(servicePort)
  }

  def "should sign in and fetch accounts info asynchronously on successful scraper facade invocations"() {
    given:
    stubTimeToNow()

    and:
    CountDownLatch importLatch = new CountDownLatch(1)

    and:
    stubAccounts {
      importLatch.await()
      new Accounts([
        Account.from("account-1", "1000.00", "PLN"),
        Account.from("account-2", "2000.00", "EUR"),
        Account.from("account-3", "3000.00", "USD")
      ])
    }

    when:
    HttpResponseWrapper logInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    then:
    logInResponse.statusCode == HttpStatus.OK

    when:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(CORRECT_OTP, logInResponse.extractSessionId())
    String importId = otpResponse.extractImportId()

    then:
    otpResponse.statusCode == HttpStatus.OK
    importId != null

    when: 'until accounts fetch finishes we get NO_CONTENT response'
    HttpResponseWrapper emptyResponse = serviceClient.getAccounts(importId)

    then:
    emptyResponse.statusCode == HttpStatus.NO_CONTENT

    and:
    emptyResponse.body == null

    when: 'after accounts fetch finishes we get OK with accounts details'
    importLatch.countDown()

    then:
    poll.eventually {
      HttpResponseWrapper accountsResponse = serviceClient.getAccounts(importId)
      accountsResponse.statusCode == HttpStatus.OK
      jsonsEqual(accountsResponse.body, """
        {
          "accounts": [
            {
              "name": "account-1",
              "balance": "1000.00",
              "currency": "PLN"
            },
            {
              "name": "account-2",
              "balance": "2000.00",
              "currency": "EUR"
            },
            {
              "name": "account-3",
              "balance": "3000.00",
              "currency": "USD"
            }
          ]
        }
      """)
    }
  }

  def "should return InvalidCredentials response sign in fails"() {
    given:
    stubTimeToNow()

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(WRONG_LOGIN, WRONG_PASSWORD)

    then:
    signInResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    jsonsEqual(signInResponse.body, """
      {
        "code": "InvalidCredentials",
        "message": "Invalid credentials. Please, try again."
      }
    """)
  }

  def "should return InvalidCredentials response in case otp input fails"() {
    given:
    stubTimeToNow()

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    and:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(WRONG_OTP, signInResponse.extractSessionId())

    then:
    otpResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    jsonsEqual(otpResponse.body, """
      {
        "code": "InvalidCredentials",
        "message": "Invalid credentials. Please, try again."
      }
    """)
  }

  def "should return internal sever error on unexpected exception during sign in"() {
    given:
    stubTimeToNow()

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(ERROR_LOGIN, ERROR_PASSWORD)

    then:
    signInResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    jsonsEqual(signInResponse.body, """
      {
        "code": "Error",
        "message": "Unexpected error occurred."
      }
    """)
  }

  def "should return internal sever error on unexpected exception during otp"() {
    given:
    stubTimeToNow()

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    and:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(ERROR_OTP, signInResponse.extractSessionId())

    then:
    otpResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    jsonsEqual(otpResponse.body, """
      {
        "code": "Error",
        "message": "Unexpected error occurred."
      }
    """)
  }

  def "should return info about failed import when accounts import fails for any reason"() {
    given:
    stubTimeToNow()

    and:
    stubAccounts { throw new RuntimeException() }

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    and:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(CORRECT_OTP, signInResponse.extractSessionId())

    then:
    poll.eventually {
      String importId = otpResponse.extractImportId()
      HttpResponseWrapper accountsResponse = serviceClient.getAccounts(importId)
      accountsResponse.statusCode == HttpStatus.OK
      jsonsEqual(accountsResponse.body, """
        {
          "message": "Import ['$importId'] failed."
        }
      """)
    }
  }

  def "should retain otp required session for up to 5 minutes"() {
    given:
    stubTimeTo(Instant.now().minus(5, MINUTES))

    and:
    stubDefaultAccounts()

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)
    String sessionId = signInResponse.extractSessionId()

    then:
    retentionPoll.eventually {
      HttpResponseWrapper otpResponse = serviceClient.postOtp(CORRECT_OTP, sessionId)
      jsonsEqual(otpResponse.body, """
        {
          "code": "SessionNotFound",
          "message": "Session with id [$sessionId] not found. Make sure proper x-session header is set."
        }
      """)
    }
  }

  def "should retain imported accounts for up to 24 hours"() {
    given:
    stubTimeTo(Instant.now().minus(24, HOURS))

    and:
    stubDefaultAccounts()

    when:
    HttpResponseWrapper logInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)
    HttpResponseWrapper otpResponse = serviceClient.postOtp(CORRECT_OTP, logInResponse.extractSessionId())
    String importId = otpResponse.extractImportId()

    then:
    poll.eventually {
      HttpResponseWrapper accountsResponse = serviceClient.getAccounts(importId)
      accountsResponse.statusCode == HttpStatus.OK
      accountsResponse.body != null
    }

    and:
    retentionPoll.eventually {
      HttpResponseWrapper emptyResponse = serviceClient.getAccounts(importId)
      emptyResponse.statusCode == HttpStatus.NO_CONTENT
      emptyResponse.body == null
    }
  }
}
