package com.kontomatik.pko.service.persistence

import com.kontomatik.pko.lib.usecase.login.FlowId
import com.kontomatik.pko.lib.usecase.login.PkoSessionId
import com.kontomatik.pko.lib.usecase.login.Token
import com.kontomatik.pko.service.domain.session.FinishedSession
import com.kontomatik.pko.service.domain.session.InitialSession
import com.kontomatik.pko.service.domain.session.LoggedInSession
import com.kontomatik.pko.service.domain.session.LoginInProgressSession
import com.kontomatik.pko.service.domain.session.OwnerId
import com.kontomatik.pko.service.domain.session.OwnerSessionId
import spock.lang.Specification
import spock.lang.Subject

class InMemoryOwnerSessionRepositorySpec extends Specification {

  @Subject
  InMemoryOwnerSessionRepository repository = new InMemoryOwnerSessionRepository()

  private static GENERIC_OWNER_SESSION_ID = new OwnerSessionId("test-owner-session-id")
  private static GENERIC_OTHER_OWNER_SESSION_ID = new OwnerSessionId("test-owner-session-id")
  private static GENERIC_OWNER_ID = new OwnerId("test-owner-id")

  private static InitialSession genericInitialOwnerSession(OwnerSessionId ownerSessionId) {
    return new InitialSession(
      ownerSessionId,
      GENERIC_OWNER_ID
    )
  }

  private static GENERIC_PKO_SESSION_ID = new PkoSessionId("test-pko-session-id")
  private static GENERIC_FLOW_ID = new FlowId("test-flow-id")
  private static GENERIC_TOKEN = new Token("test-token")

  private static LoginInProgressSession genericLoginInProgressOwnerSession(OwnerSessionId ownerSessionId) {
    new LoginInProgressSession(
      ownerSessionId,
      GENERIC_OWNER_ID,
      GENERIC_PKO_SESSION_ID,
      GENERIC_FLOW_ID,
      GENERIC_TOKEN
    )
  }

  private static LoggedInSession genericLoggedInOwnerSession(OwnerSessionId ownerSessionId) {
    new LoggedInSession(
      ownerSessionId,
      GENERIC_OWNER_ID,
      GENERIC_PKO_SESSION_ID
    )
  }

  private static FinishedSession genericFinishedOwnerSession(OwnerSessionId ownerSessionId) {
    return new FinishedSession(
      ownerSessionId,
      GENERIC_OWNER_ID
    )
  }

  def "should fetch stored initial owner session by id"() {
    given:
    repository.store(genericInitialOwnerSession(GENERIC_OWNER_SESSION_ID))
    repository.store(genericInitialOwnerSession(GENERIC_OTHER_OWNER_SESSION_ID))

    expect:
    with(repository.fetchInitialOwnerSession(GENERIC_OWNER_SESSION_ID).get()) {
      it.ownerSessionId() == GENERIC_OWNER_SESSION_ID
      it.ownerId() == GENERIC_OWNER_ID
    }
  }

  def "should fetch stored login in progress owner session by id"() {
    given:
    repository.store(genericLoginInProgressOwnerSession(GENERIC_OWNER_SESSION_ID))
    repository.store(genericLoginInProgressOwnerSession(GENERIC_OTHER_OWNER_SESSION_ID))

    expect:
    with(repository.fetchLoginInProgressOwnerSession(GENERIC_OWNER_SESSION_ID).get()) {
      it.ownerSessionId() == GENERIC_OWNER_SESSION_ID
      it.ownerId() == GENERIC_OWNER_ID
      it.pkoSessionId() == GENERIC_PKO_SESSION_ID
      it.flowId() == GENERIC_FLOW_ID
      it.token() == GENERIC_TOKEN
    }
  }

  def "should fetch stored logged in owner session by id"() {
    given:
    repository.store(genericLoggedInOwnerSession(GENERIC_OWNER_SESSION_ID))
    repository.store(genericLoggedInOwnerSession(GENERIC_OTHER_OWNER_SESSION_ID))

    expect:
    with(repository.fetchLoggedInOwnerSession(GENERIC_OWNER_SESSION_ID).get()) {
      it.ownerSessionId() == GENERIC_OWNER_SESSION_ID
      it.ownerId() == GENERIC_OWNER_ID
      it.pkoSessionId() == GENERIC_PKO_SESSION_ID
    }
  }

  def "storing login in progress owner session should clear corresponding initial owner session"() {
    given:
    repository.store(genericInitialOwnerSession(GENERIC_OWNER_SESSION_ID))

    when:
    repository.store(genericLoginInProgressOwnerSession(GENERIC_OWNER_SESSION_ID))

    then:
    repository.fetchInitialOwnerSession(GENERIC_OWNER_SESSION_ID).isEmpty()
  }

  def "storing logged in owner session should clear corresponding login in progress owner session"() {
    given:
    repository.store(genericLoginInProgressOwnerSession(GENERIC_OWNER_SESSION_ID))

    when:
    repository.store(genericLoggedInOwnerSession(GENERIC_OWNER_SESSION_ID))

    then:
    repository.fetchLoginInProgressOwnerSession(GENERIC_OWNER_SESSION_ID).isEmpty()
  }

  def "storing finished owner session should clear corresponding logged in owner session"() {
    given:
    repository.store(genericLoggedInOwnerSession(GENERIC_OWNER_SESSION_ID))

    when:
    repository.store(genericFinishedOwnerSession(GENERIC_OWNER_SESSION_ID))

    then:
    repository.fetchLoggedInOwnerSession(GENERIC_OWNER_SESSION_ID).isEmpty()
  }

  def "should not fetch nonexistent initial owner session"() {
    given:
    repository.store(genericInitialOwnerSession(GENERIC_OWNER_SESSION_ID))
    repository.store(genericInitialOwnerSession(GENERIC_OTHER_OWNER_SESSION_ID))

    expect:
    repository.fetchInitialOwnerSession(new OwnerSessionId("nonexistent")).isEmpty()
  }

  def "should not fetch nonexistent login in progress owner session"() {
    given:
    repository.store(genericLoginInProgressOwnerSession(GENERIC_OWNER_SESSION_ID))
    repository.store(genericLoginInProgressOwnerSession(GENERIC_OTHER_OWNER_SESSION_ID))

    expect:
    repository.fetchLoginInProgressOwnerSession(new OwnerSessionId("nonexistent")).isEmpty()
  }

  def "should not fetch nonexistent logged in owner session"() {
    repository.store(genericLoggedInOwnerSession(GENERIC_OWNER_SESSION_ID))
    repository.store(genericLoggedInOwnerSession(GENERIC_OTHER_OWNER_SESSION_ID))

    expect:
    repository.fetchLoggedInOwnerSession(new OwnerSessionId("nonexistent")).isEmpty()
  }
}
