package com.kontomatik.service.pko

import com.kontomatik.lib.pko.ScraperFacade
import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.signin.*
import com.kontomatik.service.BaseIntegrationSpec
import org.spockframework.spring.SpringBean

abstract class ScraperFacadeMockBaseSpec extends BaseIntegrationSpec {

  final String CORRECT_LOGIN = "test-login"
  final String CORRECT_PASSWORD = "test-password"
  final String CORRECT_OTP = "correct-otp"

  final String WRONG_LOGIN = "wrong-login"
  final String WRONG_PASSWORD = "wrong-password"
  final String WRONG_OTP = "wrong-otp"

  final String ERROR_LOGIN = "error-login"
  final String ERROR_PASSWORD = "error-password"
  final String ERROR_OTP = "error-otp"

  @SpringBean
  ScraperFacade pkoScraperFacade = Stub()

  def setup() {
    stubDefaultSignIn()
    stubDefaultOtp()
  }

  void stubDefaultSignIn() {
    stubSignIn(new Credentials(CORRECT_LOGIN, CORRECT_PASSWORD)) { testOtpRequiredPkoSession() }
    stubSignIn(new Credentials(WRONG_LOGIN, WRONG_PASSWORD)) { throw new InvalidCredentials() }
    stubSignIn(new Credentials(ERROR_LOGIN, ERROR_PASSWORD)) { throw new RuntimeException() }
  }

  void stubSignIn(
    Credentials credentials,
    Closure logInClosure
  ) {
    this.pkoScraperFacade.signIn(credentials) >> { logInClosure() }
  }

  void stubDefaultOtp() {
    stubOtp(new Otp(CORRECT_OTP)) { testLoggedInPkoSession() }
    stubOtp(new Otp(WRONG_OTP)) { throw new InvalidCredentials() }
    stubOtp(new Otp(ERROR_OTP)) { throw new RuntimeException() }
  }

  void stubOtp(
    Otp otp,
    Closure otpClosure
  ) {
    this.pkoScraperFacade.inputOtp(testOtpRequiredPkoSession(), otp) >> { otpClosure() }
  }

  void stubDefaultAccounts() {
    stubAccounts { new Accounts([]) }
  }

  void stubAccounts(
    Closure accountsClosure
  ) {
    this.pkoScraperFacade.fetchAccounts(testLoggedInPkoSession()) >> { accountsClosure() }
  }

  static PkoSessionId testPkoSessionId() {
    return new PkoSessionId("test-pko-session-id")
  }

  static OtpRequiredPkoSession testOtpRequiredPkoSession() {
    return new OtpRequiredPkoSession(
      testPkoSessionId(),
      new FlowId("test-flow-id"),
      new Token("test-token")
    )
  }

  static LoggedInPkoSession testLoggedInPkoSession() {
    return new LoggedInPkoSession(
      testPkoSessionId()
    )
  }
}
