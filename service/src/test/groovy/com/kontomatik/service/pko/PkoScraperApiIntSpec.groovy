package com.kontomatik.service.pko

import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CountDownLatch

import static ServiceClient.extractSessionId

class PkoScraperApiIntSpec extends ScraperFacadeMockSpec {

  PollingConditions poll = new PollingConditions(timeout: 1)
  ServiceClient serviceClient

  def setup() {
    serviceClient = new ServiceClient(servicePort)
  }

  def "should sign in and fetch accounts info asynchronously on successful scraper facade invocations"() {
    given:
    CountDownLatch importLatch = new CountDownLatch(1)

    and:
    stubAccounts {
      importLatch.await()
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
    HttpResponseWrapper logInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    then:
    logInResponse.statusCode == HttpStatus.OK

    when:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(CORRECT_OTP, extractSessionId(logInResponse))

    then:
    otpResponse.statusCode == HttpStatus.OK

    when: 'until accounts fetch finishes we get empty OK response'
    HttpResponseWrapper emptyResponse = serviceClient.getAccounts(extractSessionId(otpResponse))

    then:
    emptyResponse.statusCode == HttpStatus.OK

    and:
    emptyResponse.body == null

    when: 'after accounts fetch finishes we get OK with accounts details'
    importLatch.countDown()

    then: 'until accounts fetch finishes we get empty OK response'
    poll.eventually {
      HttpResponseWrapper accountsResponse = serviceClient.getAccounts(extractSessionId(otpResponse))
      accountsResponse.statusCode == HttpStatus.OK
      with(new JsonSlurper().parseText(accountsResponse.body)) {
        it.data.size() == 3
        with(it.data.find { it.name == "account-1" }) {
          it.balance == "1000.00"
          it.currency == "PLN"
        }
        with(it.data.find { it.name == "account-2" }) {
          it.balance == "2000.00"
          it.currency == "EUR"
        }
        with(it.data.find { it.name == "account-3" }) {
          it.balance == "3000.00"
          it.currency == "USD"
        }
      }
    }
  }

  def "should return InvalidCredentials response sign in fails"() {
    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(WRONG_LOGIN, WRONG_PASSWORD)

    then:
    signInResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    with(new JsonSlurper().parseText(signInResponse.body)) {
      it.code == "InvalidCredentials"
    }
  }

  def "should return InvalidCredentials response in case otp input fails"() {
    given:

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    and:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(WRONG_OTP, extractSessionId(signInResponse))

    then:
    otpResponse.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    with(new JsonSlurper().parseText(otpResponse.body)) {
      it.code == "InvalidCredentials"
    }
  }

  def "should return internal sever error on unexpected exception during sign in"() {
    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(ERROR_LOGIN, ERROR_PASSWORD)

    then:
    signInResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    with(new JsonSlurper().parseText(signInResponse.body)) {
      it.code == "Error"
    }
  }

  def "should return internal sever error on unexpected exception during otp"() {
    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    and:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(ERROR_OTP, extractSessionId(signInResponse))

    then:
    otpResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    with(new JsonSlurper().parseText(otpResponse.body)) {
      it.code == "Error"
    }
  }

  def "should return info about failed import when accounts import fails for any reason"() {
    given:
    stubAccounts { throw new RuntimeException() }

    when:
    HttpResponseWrapper signInResponse = serviceClient.postSignIn(CORRECT_LOGIN, CORRECT_PASSWORD)

    and:
    HttpResponseWrapper otpResponse = serviceClient.postOtp(CORRECT_OTP, extractSessionId(signInResponse))

    then:
    poll.eventually {
      String importSessionId = extractSessionId(otpResponse)
      HttpResponseWrapper accountsResponse = serviceClient.getAccounts(importSessionId)
      accountsResponse.statusCode == HttpStatus.OK
      with(new JsonSlurper().parseText(accountsResponse.body)) {
        it.data == "Import failed for session ['$importSessionId']"
      }
    }
  }
}
