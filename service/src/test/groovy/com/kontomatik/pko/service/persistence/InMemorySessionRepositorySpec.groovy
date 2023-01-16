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

  def "should fetch stored login in progress session by id"() {
    given:
    repository.save(genericLoginInProgressSession(GENERIC_SESSION_ID))
    repository.save(genericLoginInProgressSession(GENERIC_OTHER_SESSION_ID))

    expect:
    with(repository.getLoginInProgressSession(GENERIC_SESSION_ID)) {
      it.sessionId() == GENERIC_SESSION_ID
      it.pkoSessionId() == GENERIC_PKO_SESSION_ID
      it.flowId() == GENERIC_FLOW_ID
      it.token() == GENERIC_TOKEN
    }
  }

  def "should fetch stored logged in session by id"() {
    given:
    repository.save(genericLoggedInSession(GENERIC_SESSION_ID))
    repository.save(genericLoggedInSession(GENERIC_OTHER_SESSION_ID))

    expect:
    with(repository.getLoggedInSession(GENERIC_SESSION_ID)) {
      it.sessionId() == GENERIC_SESSION_ID
      it.pkoSessionId() == GENERIC_PKO_SESSION_ID
    }
  }

  def "storing logged in session should clear corresponding login in progress session"() {
    given:
    repository.save(genericLoginInProgressSession(GENERIC_SESSION_ID))

    and:
    repository.save(genericLoggedInSession(GENERIC_SESSION_ID))

    when:
    repository.getLoginInProgressSession(GENERIC_SESSION_ID) == null

    then:
    thrown(SessionLoginNotInProgress)
  }

  def "storing finished session should clear corresponding logged in session"() {
    given:
    repository.save(genericLoggedInSession(GENERIC_SESSION_ID))

    and:
    repository.save(genericFinishedSession(GENERIC_SESSION_ID))

    when:
    repository.getLoggedInSession(GENERIC_SESSION_ID) == null

    then:
    thrown(SessionNotLoggedIn)
  }

  def "should not fetch nonexistent login in progress session"() {
    given:
    repository.save(genericLoginInProgressSession(GENERIC_SESSION_ID))
    repository.save(genericLoginInProgressSession(GENERIC_OTHER_SESSION_ID))

    when:
    repository.getLoginInProgressSession(new SessionId("nonexistent"))

    then:
    thrown(SessionLoginNotInProgress)
  }

  def "should not fetch nonexistent logged in session"() {
    repository.save(genericLoggedInSession(GENERIC_SESSION_ID))
    repository.save(genericLoggedInSession(GENERIC_OTHER_SESSION_ID))

    when:
    repository.getLoggedInSession(new SessionId("nonexistent"))

    then:
    thrown(SessionNotLoggedIn)
  }
}
