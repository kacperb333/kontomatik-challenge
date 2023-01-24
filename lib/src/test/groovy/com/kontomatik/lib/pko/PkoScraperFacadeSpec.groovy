package com.kontomatik.lib.pko

import com.github.tomakehurst.wiremock.WireMockServer
import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.login.Credentials
import com.kontomatik.lib.pko.domain.login.LoggedInPkoSession
import com.kontomatik.lib.pko.domain.login.LoginInProgressPkoSession
import com.kontomatik.lib.pko.domain.login.Otp
import spock.lang.Specification
import spock.lang.Subject

import static com.kontomatik.lib.pko.PkoApiFixtures.*

class PkoScraperFacadeSpec extends Specification {

  PkoApiFixtures pkoApi = new PkoApiFixtures(new WireMockServer(8090))

  @Subject
  PkoScraperFacade pkoScraperFacade = PkoScraperFacadeFactory.pkoScraperFacade("http://localhost:8090/ipko3")

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
    LoginInProgressPkoSession loginInProgressSession = pkoScraperFacade.logIn(new Credentials("test-login", "test-password"))

    and:
    LoggedInPkoSession loggedInPkoSession = pkoScraperFacade.inputOtp(loginInProgressSession, new Otp("test-otp"))

    and:
    Accounts fetchedAccounts = pkoScraperFacade.fetchAccounts(loggedInPkoSession)

    then:
    fetchedAccounts == expectedAccounts
  }

  def "should throw exception on erroneous pko login response"() {
    given:
    pkoApi.stubPkoLogin("test-login", testedLoginResponse)

    when:
    pkoScraperFacade.logIn(new Credentials("test-login", "test-password"))

    then:
    thrown(expectedThrown)

    where:
    testedLoginResponse          || expectedThrown
    wrongLoginResponse()         || PkoScraperFacade.LoginFailed
    malformedResponse()          || PkoScraperFacade.PkoScraperFacadeBug
    badRequestResponse()         || PkoScraperFacade.PkoScraperFacadeBug
    serviceUnavailableResponse() || PkoScraperFacade.PkoScraperFacadeBug
  }

  def "should throw exception on erroneous password response"() {
    given:
    pkoApi.stubPkoLogin("test-login", successfulLoginResponse())
    pkoApi.stubPkoPassword("test-password", testedPasswordResponse)

    when:
    pkoScraperFacade.logIn(new Credentials("test-login", "test-password"))

    then:
    thrown(expectedThrown)

    where:
    testedPasswordResponse       || expectedThrown
    wrongPasswordResponse()      || PkoScraperFacade.LoginFailed
    malformedResponse()          || PkoScraperFacade.PkoScraperFacadeBug
    badRequestResponse()         || PkoScraperFacade.PkoScraperFacadeBug
    serviceUnavailableResponse() || PkoScraperFacade.PkoScraperFacadeBug
  }

  def "should throw exception on erroneous otp response"() {
    given:
    pkoApi.stubPkoLogin("test-login", successfulLoginResponse())
    pkoApi.stubPkoPassword("test-password", successfulPasswordResponse())
    pkoApi.stubPkoOtp("wrong-otp", testedOtpResponse)

    when:
    LoginInProgressPkoSession loginInProgressSession = pkoScraperFacade.logIn(new Credentials("test-login", "test-password"))

    and:
    pkoScraperFacade.inputOtp(loginInProgressSession, new Otp("wrong-otp"))

    then:
    thrown(expectedThrown)

    where:
    testedOtpResponse            || expectedThrown
    wrongOtpResponse()           || PkoScraperFacade.LoginFailed
    malformedResponse()          || PkoScraperFacade.PkoScraperFacadeBug
    badRequestResponse()         || PkoScraperFacade.PkoScraperFacadeBug
    serviceUnavailableResponse() || PkoScraperFacade.PkoScraperFacadeBug
  }

  def "should throw exception on accounts fetch error"() {
    given:
    pkoApi.stubPkoLogin("test-login", successfulLoginResponse())
    pkoApi.stubPkoPassword("test-password", successfulPasswordResponse())
    pkoApi.stubPkoOtp("test-otp", successfulOtpResponse())
    pkoApi.stubPkoAccounts(testedAccountsResponse)

    when:
    LoginInProgressPkoSession loginInProgressSession = pkoScraperFacade.logIn(new Credentials("test-login", "test-password"))

    and:
    LoggedInPkoSession loggedInPkoSession = pkoScraperFacade.inputOtp(loginInProgressSession, new Otp("test-otp"))

    and:
    pkoScraperFacade.fetchAccounts(loggedInPkoSession)

    then:
    thrown(expectedThrown)

    where:
    testedAccountsResponse       || expectedThrown
    malformedResponse()          || PkoScraperFacade.PkoScraperFacadeBug
    badRequestResponse()         || PkoScraperFacade.PkoScraperFacadeBug
    serviceUnavailableResponse() || PkoScraperFacade.PkoScraperFacadeBug
  }
}
