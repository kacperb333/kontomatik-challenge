package com.kontomatik.pko.service.persistence

import com.kontomatik.pko.lib.usecase.login.FlowId
import com.kontomatik.pko.lib.usecase.login.PkoSessionId
import com.kontomatik.pko.lib.usecase.login.Token
import com.kontomatik.pko.service.domain.session.*
import spock.lang.Specification
import spock.lang.Subject

class InMemorySessionRepositorySpec extends Specification {

  @Subject
  InMemorySessionRepository repository = new InMemorySessionRepository()

  private static GENERIC_SESSION_ID = new SessionId("test-session-id")
  private static GENERIC_OTHER_SESSION_ID = new SessionId("test-session-id")

  private static InitialSession genericInitialSession(SessionId sessionId) {
    return new InitialSession(sessionId)
  }

  private static GENERIC_PKO_SESSION_ID = new PkoSessionId("test-pko-session-id")
  private static GENERIC_FLOW_ID = new FlowId("test-flow-id")
  private static GENERIC_TOKEN = new Token("test-token")

  private static LoginInProgressSession genericLoginInProgressSession(SessionId sessionId) {
    new LoginInProgressSession(sessionId, GENERIC_PKO_SESSION_ID, GENERIC_FLOW_ID, GENERIC_TOKEN)
  }

  private static LoggedInSession genericLoggedInSession(SessionId sessionId) {
    new LoggedInSession(sessionId, GENERIC_PKO_SESSION_ID)
  }

  private static FinishedSession genericFinishedSession(SessionId sessionId) {
    return new FinishedSession(sessionId)
  }

  def "should fetch stored initial session by id"() {
    given:
    repository.store(genericInitialSession(GENERIC_SESSION_ID))
    repository.store(genericInitialSession(GENERIC_OTHER_SESSION_ID))

    expect:
    with(repository.fetchInitialSession(GENERIC_SESSION_ID).get()) {
      it.SessionId() == GENERIC_SESSION_ID
    }
  }

  def "should fetch stored login in progress session by id"() {
    given:
    repository.store(genericLoginInProgressSession(GENERIC_SESSION_ID))
    repository.store(genericLoginInProgressSession(GENERIC_OTHER_SESSION_ID))

    expect:
    with(repository.fetchLoginInProgressSession(GENERIC_SESSION_ID).get()) {
      it.sessionId() == GENERIC_SESSION_ID
      it.pkoSessionId() == GENERIC_PKO_SESSION_ID
      it.flowId() == GENERIC_FLOW_ID
      it.token() == GENERIC_TOKEN
    }
  }

  def "should fetch stored logged in session by id"() {
    given:
    repository.store(genericLoggedInSession(GENERIC_SESSION_ID))
    repository.store(genericLoggedInSession(GENERIC_OTHER_SESSION_ID))

    expect:
    with(repository.fetchLoggedInSession(GENERIC_SESSION_ID).get()) {
      it.sessionId() == GENERIC_SESSION_ID
      it.pkoSessionId() == GENERIC_PKO_SESSION_ID
    }
  }

  def "storing login in progress session should clear corresponding initial session"() {
    given:
    repository.store(genericInitialSession(GENERIC_SESSION_ID))

    when:
    repository.store(genericLoginInProgressSession(GENERIC_SESSION_ID))

    then:
    repository.fetchInitialSession(GENERIC_SESSION_ID).isEmpty()
  }

  def "storing logged in session should clear corresponding login in progress session"() {
    given:
    repository.store(genericLoginInProgressSession(GENERIC_SESSION_ID))

    when:
    repository.store(genericLoggedInSession(GENERIC_SESSION_ID))

    then:
    repository.fetchLoginInProgressSession(GENERIC_SESSION_ID).isEmpty()
  }

  def "storing finished session should clear corresponding logged in session"() {
    given:
    repository.store(genericLoggedInSession(GENERIC_SESSION_ID))

    when:
    repository.store(genericFinishedSession(GENERIC_SESSION_ID))

    then:
    repository.fetchLoggedInSession(GENERIC_SESSION_ID).isEmpty()
  }

  def "should not fetch nonexistent initial session"() {
    given:
    repository.store(genericInitialSession(GENERIC_SESSION_ID))
    repository.store(genericInitialSession(GENERIC_OTHER_SESSION_ID))

    expect:
    repository.fetchInitialSession(new SessionId("nonexistent")).isEmpty()
  }

  def "should not fetch nonexistent login in progress session"() {
    given:
    repository.store(genericLoginInProgressSession(GENERIC_SESSION_ID))
    repository.store(genericLoginInProgressSession(GENERIC_OTHER_SESSION_ID))

    expect:
    repository.fetchLoginInProgressSession(new SessionId("nonexistent")).isEmpty()
  }

  def "should not fetch nonexistent logged in session"() {
    repository.store(genericLoggedInSession(GENERIC_SESSION_ID))
    repository.store(genericLoggedInSession(GENERIC_OTHER_SESSION_ID))

    expect:
    repository.fetchLoggedInSession(new SessionId("nonexistent")).isEmpty()
  }
}
