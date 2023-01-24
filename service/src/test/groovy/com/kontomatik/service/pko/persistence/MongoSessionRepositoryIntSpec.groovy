package com.kontomatik.service.pko.persistence


import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.service.IntegrationSpec
import com.kontomatik.service.pko.domain.*
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Subject

import static com.kontomatik.service.pko.PkoLibFactories.testAccounts
import static com.kontomatik.service.pko.PkoLibFactories.testOtpRequiredPkoSession

class MongoSessionRepositoryIntSpec extends IntegrationSpec {

  @Subject
  @Autowired
  MongoSessionRepository repository

  static SessionId testSessionId() {
    return new SessionId("test-session-id")
  }

  def "should read saved otp required session"() {
    given:
    OtpRequiredSession savedSession = new OtpRequiredSession(testSessionId(), testOtpRequiredPkoSession())

    when:
    repository.save(savedSession)

    then:
    repository.getOtpRequiredSession(testSessionId()) == savedSession
  }

  def "should throw SessionNotFound when getting nonexistent otp required session"() {
    when:
    repository.getOtpRequiredSession(new SessionId("nonexistent"))

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
    new OtpRequiredSession(testSessionId(), testOtpRequiredPkoSession()) || Accounts.EMPTY || false
    new ImportFailedSession(testSessionId())                                     || Accounts.EMPTY       || true
    new ImportFinishedSession(testSessionId(), testAccounts())                   || testAccounts()       || false
  }

  def "should throw SessionNotFound when looking for otp required session, after saving finished session with the same id"() {
    given:
    repository.save(new OtpRequiredSession(testSessionId(), testOtpRequiredPkoSession()))

    and:
    repository.save(testFinishedSession)

    when:
    repository.getOtpRequiredSession(testSessionId())

    then:
    thrown(SessionNotFound)

    where:
    testFinishedSession << [
      new ImportFinishedSession(testSessionId(), testAccounts()),
      new ImportFailedSession(testSessionId())
    ]
  }
}
