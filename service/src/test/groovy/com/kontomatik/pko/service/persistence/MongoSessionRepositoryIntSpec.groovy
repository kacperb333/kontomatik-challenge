package com.kontomatik.pko.service.persistence

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo
import com.kontomatik.pko.lib.usecase.login.FlowId
import com.kontomatik.pko.lib.usecase.login.LoginInProgressPkoSession
import com.kontomatik.pko.lib.usecase.login.PkoSessionId
import com.kontomatik.pko.lib.usecase.login.Token
import com.kontomatik.pko.service.IntegrationSpec
import com.kontomatik.pko.service.domain.ImportFailedSession
import com.kontomatik.pko.service.domain.ImportFinishedSession
import com.kontomatik.pko.service.domain.LoginInProgressSession
import com.kontomatik.pko.service.domain.SessionId
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Subject

import java.time.Instant
import java.time.ZonedDateTime

class MongoSessionRepositoryIntSpec extends IntegrationSpec {

  @Subject
  @Autowired
  MongoSessionRepository repository

  static Instant now() {
    return ZonedDateTime.parse("2020-01-01T05:00:00+01:00").toInstant()
  }

  static SessionId testSessionId() {
    return new SessionId("test-session-id")
  }

  static LoginInProgressPkoSession testLoginInProgressPkoSession() {
    return new LoginInProgressPkoSession(
      new PkoSessionId("test-pko-session-id"),
      new FlowId("test-flow-id"),
      new Token("test-token")
    )
  }

  def "should read saved login in progress session"() {
    given:
    LoginInProgressSession savedSession =
      new LoginInProgressSession(testSessionId(), testLoginInProgressPkoSession())

    and:
    repository.save(savedSession)

    expect:
    repository.getLoginInProgressSession(testSessionId()) == savedSession
  }

  def "should read saved import finished session"() {
    given:
    ImportFinishedSession savedSession = new ImportFinishedSession(
      testSessionId(),
      now(),
      new AccountsInfo([
        new AccountInfo("account-1", "31.00", "PLN"),
        new AccountInfo("account-2", "32.00", "USD")
      ])
    )

    and:
    repository.save(savedSession)

    expect:
    repository.getFinishedSession(testSessionId()) == savedSession
  }

  def "should read saved import failed session"() {
    given:
    ImportFailedSession savedSession = new ImportFailedSession(testSessionId(), now())

    and:
    repository.save(savedSession)

    expect:
    repository.getFinishedSession(testSessionId()) == savedSession
  }
}
