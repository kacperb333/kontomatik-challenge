package com.kontomatik.pko

import spock.lang.Specification
import spock.lang.Subject

class PkoScraperFacadeSpec extends Specification {

    PkoClient pkoClient = Mock()
    PkoLoginUseCase pkoLoginUseCase = new PkoLoginUseCase(pkoClient)
    PkoAccountInfoUseCase pkoAccountInfoUseCase = new PkoAccountInfoUseCase(pkoClient)

    @Subject
    PkoScraperFacade pkoScraperFacade = new PkoScraperFacade(
        pkoLoginUseCase,
        pkoAccountInfoUseCase
    )

    private static EXPECTED_LOGIN_ASSERTION_DATA = new PkoClient.PkoInProgressLoginAssertionData("password")
    private static EXPECTED_PASSWORD_ASSERTION_DATA = new PkoClient.PkoInProgressLoginAssertionData("one_time_password")
    private static EXPECTED_OTP_ASSERTION_DATA = new PkoClient.PkoSuccessfulLoginAssertionData("END", true)

    private static GENERIC_PKO_SESSION_ID = new PkoSessionId("test-pko-session-id")
    private static GENERIC_LOGIN_IN_PROGRESS_FLOW = new PkoClient.PkoLoginInProgressFlow(
        GENERIC_PKO_SESSION_ID,
        new FlowId("test-login-flow-id"),
        new Token("test-token")
    )
    private static GENERIC_LOGIN_IN_PROGRESS_PKO_SESSION = new LoginInProgressPkoSession(
        GENERIC_PKO_SESSION_ID,
        new FlowId("test-login-flow-id"),
        new Token("test-token")
    )

    def "should return in progress login pko session when username and password flow goes according to plan"() {
        given:
        PkoClient.PkoLoginInProgressFlow returnedLoginFlow = GENERIC_LOGIN_IN_PROGRESS_FLOW

        and:
        pkoClient.inputUserName(new PkoClient.PkoUserNameInput("test-login")) >>
            new PkoClient.PkoInProgressLoginResult(
                returnedLoginFlow,
                EXPECTED_LOGIN_ASSERTION_DATA
            )

        and:
        pkoClient.inputPassword(new PkoClient.PkoPasswordInput(returnedLoginFlow, "test-password")) >>
            new PkoClient.PkoInProgressLoginResult(
                returnedLoginFlow,
                EXPECTED_PASSWORD_ASSERTION_DATA
            )

        and:
        Credentials credentials = new Credentials("test-login", "test-password")

        when:
        LoginInProgressPkoSession inProgressPkoSession = pkoScraperFacade.logIn(credentials)

        then:
        with(inProgressPkoSession) {
            it.pkoSessionId() == new PkoSessionId("test-pko-session-id")
            it.flowId() == new FlowId("test-login-flow-id")
            it.token() == new Token("test-token")
        }
    }

    def "should return successful login pko session when otp flow goes according to plan"() {
        given:
        PkoClient.PkoLoginInProgressFlow assumedInProgressLoginFlow = GENERIC_LOGIN_IN_PROGRESS_FLOW

        and:
        pkoClient.inputOtp(new PkoClient.PkoOtpInput(assumedInProgressLoginFlow, "69240")) >>
            new PkoClient.PkoSuccessfulLoginResult(
                new PkoSessionId("test-pko-session-id"),
                EXPECTED_OTP_ASSERTION_DATA
            )

        and:
        LoginInProgressPkoSession assumedInProgressSession = GENERIC_LOGIN_IN_PROGRESS_PKO_SESSION

        when:
        LoggedInPkoSession loggedInPkoSession =
            pkoScraperFacade.inputOtp(assumedInProgressSession, new Otp("69240"))

        then:
        with(loggedInPkoSession) {
            it.pkoSessionId() == new PkoSessionId("test-pko-session-id")
        }
    }

    def "should fetch account info for logged in session"() {
        given:
        PkoSessionId assumedLoggedInPkoSession = GENERIC_PKO_SESSION_ID

        and:
        pkoClient.fetchAccounts(new PkoClient.PkoFetchAccountsInput(assumedLoggedInPkoSession)) >>
            new AccountsInfo([
                new AccountInfo("account-1", "664.32", "PLN"),
                new AccountInfo("account-2", "512.22", "EUR")
            ])

        when:
        AccountsInfo accountsInfo =
            pkoScraperFacade.fetchAccountsInfo(new LoggedInPkoSession(new PkoSessionId("test-pko-session-id")))

        then:
        with(accountsInfo) {
            it.accounts().size() == 2
            with(it.accounts().find { it.name() == "account-1" }) {
                it.balance() == "664.32"
                it.currency() == "PLN"
            }
            with(it.accounts().find { it.name() == "account-2" }) {
                it.balance() == "512.22"
                it.currency() == "EUR"
            }
        }
    }

    def "should throw UnexpectedAction when username flow does not go according to plan"() {
        given:
        pkoClient.inputUserName(new PkoClient.PkoUserNameInput("test-login")) >>
            new PkoClient.PkoInProgressLoginResult(
                GENERIC_LOGIN_IN_PROGRESS_FLOW,
                new PkoClient.PkoInProgressLoginAssertionData("unexpected-username-state-id")
            )

        when:
        pkoScraperFacade.logIn(new Credentials("test-login", "whatever-password"))

        then:
        UnexpectedAction ex = thrown(UnexpectedAction)
        with(ex) {
            it.lastStep == "username"
        }
    }

    def "should throw UnexpectedAction when password flow does not go according to plan"() {
        given:
        pkoClient.inputUserName(new PkoClient.PkoUserNameInput("test-login")) >>
            new PkoClient.PkoInProgressLoginResult(
                GENERIC_LOGIN_IN_PROGRESS_FLOW,
                EXPECTED_LOGIN_ASSERTION_DATA
            )

        and:
        pkoClient.inputPassword(new PkoClient.PkoPasswordInput(GENERIC_LOGIN_IN_PROGRESS_FLOW, "test-password")) >>
            new PkoClient.PkoInProgressLoginResult(
                GENERIC_LOGIN_IN_PROGRESS_FLOW,
                new PkoClient.PkoInProgressLoginAssertionData("unexpected-password-state-id")
            )

        when:
        pkoScraperFacade.logIn(new Credentials("test-login", "test-password"))

        then:
        UnexpectedAction ex = thrown(UnexpectedAction)
        with(ex) {
            it.lastStep == "password"
        }
    }

    def "should throw UnexpectedAction when otp flow does not go according to plan"() {
        given:
        pkoClient.inputOtp(new PkoClient.PkoOtpInput(GENERIC_LOGIN_IN_PROGRESS_FLOW, "69240")) >>
            new PkoClient.PkoSuccessfulLoginResult(
                GENERIC_PKO_SESSION_ID,
                new PkoClient.PkoSuccessfulLoginAssertionData("unexpected-otp-state-id", false)
            )

        when:
        pkoScraperFacade.inputOtp(GENERIC_LOGIN_IN_PROGRESS_PKO_SESSION, new Otp("69240"))

        then:
        UnexpectedAction ex = thrown(UnexpectedAction)
        with(ex) {
            it.lastStep == "otp"
        }
    }
}
