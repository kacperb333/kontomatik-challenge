package com.kontomatik.service.pko.persistence


import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.service.IntegrationSpec
import com.kontomatik.service.pko.domain.*
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Subject

import static com.kontomatik.service.pko.PkoLibFactories.testAccounts
import static com.kontomatik.service.pko.PkoLibFactories.testLoginInProgressPkoSession

class MongoSessionRepositoryIntSpec extends IntegrationSpec {

  @Subject
  @Autowired
  MongoSessionRepository repository

  static SessionId testSessionId() {
    return new SessionId("test-session-id")
  }

  def "should read saved login in progress session"() {
    given:
    LoginInProgressSession savedSession = new LoginInProgressSession(testSessionId(), testLoginInProgressPkoSession())

    when:
    repository.save(savedSession)

    then:
    repository.getLoginInProgressSession(testSessionId()) == savedSession
  }

  def "should throw SessionNotFound when getting nonexistent login in progress session"() {
    when:
    repository.getLoginInProgressSession(new SessionId("nonexistent"))

    then:
    thrown(SessionNotFound)
  }

  def "should throw SessionNotFound when getting accounts info for nonexistent session"() {
    when:
    repository.getSessionAccountsImport(new SessionId("nonexistent"))

    then:
    thrown(SessionNotFound)
  }

  def "should read accounts import data for every type of saved session"() {
    when:
    repository.save(testSavedSession)

    then:
    repository.getSessionAccountsImport(testSessionId()).data() == expectedAccountsRead
    repository.getSessionAccountsImport(testSessionId()).isFailed() == expectedFailed

    where:
    testSavedSession                                                             || expectedAccountsRead || expectedFailed
    new LoginInProgressSession(testSessionId(), testLoginInProgressPkoSession()) || Accounts.EMPTY       || false
    new ImportFailedSession(testSessionId())                                     || Accounts.EMPTY       || true
    new ImportFinishedSession(testSessionId(), testAccounts())                   || testAccounts()       || false
  }

  def "should throw SessionNotFound when looking for login in progress session, after saving finished session with the same id"() {
    given:
    repository.save(new LoginInProgressSession(testSessionId(), testLoginInProgressPkoSession()))

    and:
    repository.save(testFinishedSession)

    when:
    repository.getLoginInProgressSession(testSessionId())

    then:
    thrown(SessionNotFound)

    where:
    testFinishedSession << [
      new ImportFinishedSession(testSessionId(), testAccounts()),
      new ImportFailedSession(testSessionId())
    ]
  }
}
