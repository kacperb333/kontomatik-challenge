package com.kontomatik.lib.pko

import com.github.tomakehurst.wiremock.WireMockServer
import com.kontomatik.lib.pko.domain.accounts.AccountInfo
import com.kontomatik.lib.pko.domain.accounts.AccountsInfo
import com.kontomatik.lib.pko.domain.login.Credentials
import com.kontomatik.lib.pko.domain.login.LoggedInPkoSession
import com.kontomatik.lib.pko.domain.login.LoginInProgressPkoSession
import com.kontomatik.lib.pko.domain.login.Otp
import spock.lang.Specification
import spock.lang.Subject

import static PkoApiFixtures.*

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
    AccountsInfo expectedAccountsInfo = new AccountsInfo([
      new AccountInfo("account-1", "2000.00", "PLN"),
      new AccountInfo("account-2", "3000.00", "EUR")
    ])

    and:
    pkoApi.stubPkoLogin("test-login", successfulLoginResponse())
    pkoApi.stubPkoPassword("test-password", successfulPasswordResponse())
    pkoApi.stubPkoOtp("test-otp", successfulOtpResponse())
    pkoApi.stubPkoAccounts(successfulAccountsResponse(expectedAccountsInfo))

    when:
    LoginInProgressPkoSession loginInProgressSession = pkoScraperFacade.logIn(new Credentials("test-login", "test-password"))

    and:
    LoggedInPkoSession loggedInPkoSession = pkoScraperFacade.inputOtp(loginInProgressSession, new Otp("test-otp"))

    and:
    AccountsInfo fetchedAccountsInfo = pkoScraperFacade.fetchAccountsInfo(loggedInPkoSession)

    then:
    fetchedAccountsInfo == expectedAccountsInfo
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
    pkoScraperFacade.fetchAccountsInfo(loggedInPkoSession)

    then:
    thrown(expectedThrown)

    where:
    testedAccountsResponse       || expectedThrown
    malformedResponse()          || PkoScraperFacade.PkoScraperFacadeBug
    badRequestResponse()         || PkoScraperFacade.PkoScraperFacadeBug
    serviceUnavailableResponse() || PkoScraperFacade.PkoScraperFacadeBug
  }
}
