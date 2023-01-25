package com.kontomatik.lib.pko

import com.github.tomakehurst.wiremock.WireMockServer
import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.signin.*
import spock.lang.Specification
import spock.lang.Subject

import static com.kontomatik.lib.pko.PkoApiMock.*

class PkoScraperFacadeSpec extends Specification {

  PkoApiMock pkoApi = new PkoApiMock(new WireMockServer(8090))

  @Subject
  PkoScraperFacade pkoScraperFacade = PkoScraperFacade.pkoScraperFacade("http://localhost:8090/ipko3")

  def setup() {
    pkoApi.start()
  }

  def cleanup() {
    pkoApi.stop()
  }

  def "should fetch accounts info after successful login and otp input"() {
    given:
    Accounts expectedAccounts = new Accounts([
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

    and:
    pkoApi.stubPkoLogin("test-login", successfulLoginResponse())
    pkoApi.stubPkoPassword("test-password", successfulPasswordResponse())
    pkoApi.stubPkoOtp("test-otp", successfulOtpResponse())
    pkoApi.stubPkoAccounts(successfulAccountsResponse(expectedAccounts))

    when:
    OtpRequiredPkoSession otpRequiredPkoSession = pkoScraperFacade.signIn(new Credentials("test-login", "test-password"))

    and:
    LoggedInPkoSession loggedInPkoSession = pkoScraperFacade.inputOtp(otpRequiredPkoSession, new Otp("test-otp"))

    and:
    Accounts fetchedAccounts = pkoScraperFacade.fetchAccounts(loggedInPkoSession)

    then:
    fetchedAccounts == expectedAccounts
  }

  def "should throw InvalidCredentials on wrong login response"() {
    given:
    pkoApi.stubPkoLogin("test-login", wrongLoginResponse())

    when:
    pkoScraperFacade.signIn(new Credentials("test-login", "test-password"))

    then:
    thrown(InvalidCredentials)
  }

  def "should throw InvalidCredentials on wrong password response"() {
    given:
    pkoApi.stubPkoLogin("test-login", successfulLoginResponse())
    pkoApi.stubPkoPassword("test-password", wrongPasswordResponse())

    when:
    pkoScraperFacade.signIn(new Credentials("test-login", "test-password"))

    then:
    thrown(InvalidCredentials)
  }

  def "should throw InvalidCredentials on wrong otp response"() {
    given:
    pkoApi.stubPkoLogin("test-login", successfulLoginResponse())
    pkoApi.stubPkoPassword("test-password", successfulPasswordResponse())
    pkoApi.stubPkoOtp("wrong-otp", wrongOtpResponse())

    when:
    OtpRequiredPkoSession otpRequiredPkoSession = pkoScraperFacade.signIn(new Credentials("test-login", "test-password"))

    and:
    pkoScraperFacade.inputOtp(otpRequiredPkoSession, new Otp("wrong-otp"))

    then:
    thrown(InvalidCredentials)
  }
}
