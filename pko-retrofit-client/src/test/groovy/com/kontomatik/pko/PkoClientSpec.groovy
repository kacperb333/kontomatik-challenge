package com.kontomatik.pko

import com.github.tomakehurst.wiremock.WireMockServer
import retrofit2.Retrofit
import spock.lang.Specification
import spock.lang.Subject

import static com.github.tomakehurst.wiremock.client.WireMock.*

class PkoClientSpec extends Specification {

    Retrofit retrofit = PkoRetrofitClientFactory.defaultRetrofit(
        PkoRetrofitClientFactory.defaultOkHttp(),
        "http://localhost:8090/"
    )
    PkoRetrofitClient pkoRetrofitClient = PkoRetrofitClientFactory.pkoRetrofitClient(retrofit)

    @Subject
    PkoClient pkoClient = PkoRetrofitClientFactory.defaultPkoClient(pkoRetrofitClient)

    WireMockServer wireMock

    def setup() {
        wireMock = new WireMockServer(8090)
        wireMock.start()
    }

    def cleanup() {
        wireMock.stop()
    }

    def "should parse pko response for username input"() {
        given:
        wireMock.stubFor(
            post("/ipko3/login")
                .withRequestBody(equalToJson("""
                  {
                    "version": 3,
                    "state_id": "login",
                    "data": {
                      "login": "1337"
                    },
                    "action": "submit"
                  }
                """
                )).willReturn(okJson("""
                  {
                    "flow_id": "test-flow",
                    "token": "test-token",
                    "state_id": "password"
                  }
                """)
                .withHeader("x-session-id", "test-pko-session"))
        )

        when:
        PkoClient.PkoInProgressLoginResult usernameResult =
            pkoClient.inputUserName(new PkoClient.PkoUserNameInput("1337"))

        then:
        with(usernameResult) {
            with(it.loginFlow()) {
                it.pkoSessionId() == new PkoSessionId("test-pko-session")
                it.flowId() == new FlowId("test-flow")
                it.token() == new Token("test-token")
            }
            with(it.assertionData()) {
                it.stateId() == "password"
            }
        }
    }

    def "should parse pko response for password input"() {
        given:
        wireMock.stubFor(
            post("/ipko3/login")
                .withHeader("x-session-id", equalTo("test-pko-session"))
                .withRequestBody(equalToJson("""
                  {
                    "version": 3,
                    "state_id": "password",
                    "flow_id": "test-flow",
                    "token": "test-token",
                    "data": {
                      "password": "super-secret"
                    },
                    "action": "submit"
                  }
                """
                )).willReturn(okJson("""
                  {
                    "flow_id": "test-flow",
                    "token": "test-token",
                    "state_id": "one_time_password"
                  }
                """)
                .withHeader("x-session-id", "test-pko-session"))
        )

        when:
        PkoClient.PkoInProgressLoginResult passwordResult =
            pkoClient.inputPassword(new PkoClient.PkoPasswordInput(
                new PkoClient.PkoLoginInProgressFlow(
                    new PkoSessionId("test-pko-session"),
                    new FlowId("test-flow"),
                    new Token("test-token")
                ),
                "super-secret"
            ))

        then:
        with(passwordResult) {
            with(it.loginFlow()) {
                it.pkoSessionId() == new PkoSessionId("test-pko-session")
                it.flowId() == new FlowId("test-flow")
                it.token() == new Token("test-token")
            }
            with(it.assertionData()) {
                it.stateId() == "one_time_password"
            }
        }
    }

    def "should parse pko response for otp input"() {
        given:
        wireMock.stubFor(
            post("/ipko3/login")
                .withHeader("x-session-id", equalTo("test-pko-session"))
                .withRequestBody(equalToJson("""
                  {
                    "version": 3,
                    "state_id": "one_time_password",
                    "flow_id": "test-flow",
                    "token": "test-token",
                    "data": {
                      "otp": "31337"
                    },
                    "action": "submit"
                  }
                """
                )).willReturn(okJson("""
                  {
                    "state_id": "END",
                    "finished": true
                  }
                """)
                .withHeader("x-session-id", "test-pko-session"))
        )

        when:
        PkoClient.PkoSuccessfulLoginResult otpResult =
            pkoClient.inputOtp(new PkoClient.PkoOtpInput(
                new PkoClient.PkoLoginInProgressFlow(
                    new PkoSessionId("test-pko-session"),
                    new FlowId("test-flow"),
                    new Token("test-token")
                ),
                "31337"
            ))

        then:
        with(otpResult) {
            it.pkoSessionId() == new PkoSessionId("test-pko-session")
            with(it.assertionData()) {
                it.stateId() == "END"
                it.finished() == true
            }
        }
    }

    def "should parse pko response for accounts info"() {
        given:
        wireMock.stubFor(
            post("/ipko3/init")
                .withHeader("x-session-id", equalTo("test-pko-session"))
                .withRequestBody(equalToJson("""
                  {
                    "version": 3,
                    "data": {
                      "accounts": {
                        "force_synchronization": true
                      }
                    }
                  }
                """
                )).willReturn(okJson("""
                  {
                    "response": {
                      "data": {
                        "accounts": {
                          "some-account-id-1": {
                            "balance": "2000.00",
                            "currency": "PLN",
                            "name": "account-1"
                          },
                          "some-account-id-2": {
                            "balance": "3000.00",
                            "currency": "EUR",
                            "name": "account-2"
                          }
                        }
                      }
                    }
                  }
                """)
                .withHeader("x-session-id", "test-pko-session"))
        )

        when:
        AccountsInfo accountsResult =
            pkoClient.fetchAccounts(new PkoClient.PkoFetchAccountsInput(
                new PkoSessionId("test-pko-session")
            ))

        then:
        with(accountsResult.accounts()) {
            it.size() == 2
            with(it.find { it.name() == "account-1" }) {
                it.balance() == "2000.00"
                it.currency() == "PLN"
            }
            with(it.find { it.name() == "account-2" }) {
                it.balance() == "3000.00"
                it.currency() == "EUR"
            }
        }
    }
}
