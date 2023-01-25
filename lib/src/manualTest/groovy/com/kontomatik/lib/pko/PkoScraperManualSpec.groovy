package com.kontomatik.lib.pko

import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.signin.Credentials
import com.kontomatik.lib.pko.domain.signin.LoggedInPkoSession
import com.kontomatik.lib.pko.domain.signin.Otp
import com.kontomatik.lib.pko.domain.signin.OtpRequiredPkoSession
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static ManualTestUtils.readFromFile

class PkoScraperManualSpec extends Specification {

  PollingConditions pollingConditions = new PollingConditions(timeout: 30)
  ScraperFacade pkoScraperFacade = ScraperFacade.scraperFacade()

  def "should sign in and fetch accounts"() {
    given: 'login is provided via resources/login.txt file'
    String login = fetchLogin()

    and: 'password is provided via resources/password.txt file'
    String password = fetchPassword()

    and: 'login succeeds'
    OtpRequiredPkoSession otpRequiredPkoSession = pkoScraperFacade.signIn(new Credentials(login, password))

    and: 'otp is provided via resources/otp.txt file'
    String otp = fetchOtp()

    and: 'otp succeeds'
    LoggedInPkoSession loggedInPkoSession = pkoScraperFacade.inputOtp(otpRequiredPkoSession, new Otp(otp))

    when: 'accounts are fetched'
    Accounts accounts = pkoScraperFacade.fetchAccounts(loggedInPkoSession)

    then: 'some accounts data is present'
    with(accounts.list()) {
      !it.isEmpty()
      with(it.first()) {
        it.name() != null
        it.balance().amount() != null
        it.balance().currency() != null
      }
    }
  }

  private String fetchLogin() {
    return readFromFile("login.txt")
  }

  private String fetchPassword() {
    return readFromFile("password.txt")
  }

  private String fetchOtp() {
    Closure<String> otp = { readFromFile("otp.txt") }
    pollingConditions.eventually {
      assert otp() != null && otp() != ""
    }
    return otp()
  }
}
