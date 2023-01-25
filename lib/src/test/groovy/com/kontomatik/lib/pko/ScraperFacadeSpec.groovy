package com.kontomatik.lib.pko


import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.signin.*
import spock.lang.Subject

class ScraperFacadeSpec extends PkoApiMockBaseSpec {

  @Subject
  ScraperFacade pkoScraperFacade = ScraperFacade.scraperFacade("http://localhost:8090/ipko3")

  def "should fetch accounts info after successful login and otp input"() {
    when:
    OtpRequiredPkoSession otpRequiredPkoSession = pkoScraperFacade.signIn(new Credentials(CORRECT_LOGIN, CORRECT_PASSWORD))

    and:
    LoggedInPkoSession loggedInPkoSession = pkoScraperFacade.inputOtp(otpRequiredPkoSession, new Otp(CORRECT_OTP))

    and:
    Accounts fetchedAccounts = pkoScraperFacade.fetchAccounts(loggedInPkoSession)

    then:
    fetchedAccounts == CORRECT_RETURNED_ACCOUNTS
  }

  def "should throw InvalidCredentials on wrong login response"() {
    when:
    pkoScraperFacade.signIn(new Credentials(INCORRECT_LOGIN, CORRECT_PASSWORD))

    then:
    thrown(InvalidCredentials)
  }

  def "should throw InvalidCredentials on wrong password response"() {
    when:
    pkoScraperFacade.signIn(new Credentials(CORRECT_LOGIN, INCORRECT_PASSWORD))

    then:
    thrown(InvalidCredentials)
  }

  def "should throw InvalidCredentials on wrong otp response"() {
    when:
    OtpRequiredPkoSession otpRequiredPkoSession = pkoScraperFacade.signIn(new Credentials(CORRECT_LOGIN, CORRECT_PASSWORD))

    and:
    pkoScraperFacade.inputOtp(otpRequiredPkoSession, new Otp(INCORRECT_OTP))

    then:
    thrown(InvalidCredentials)
  }
}
