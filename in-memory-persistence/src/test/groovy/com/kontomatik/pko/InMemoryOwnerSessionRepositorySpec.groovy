package com.kontomatik.pko

import spock.lang.Specification
import spock.lang.Subject

class InMemoryOwnerSessionRepositorySpec extends Specification {

    @Subject
    InMemoryOwnerSessionRepository repository = new InMemoryOwnerSessionRepository()

    private static GENERIC_OWNER_SESSION_ID = new OwnerSessionId("test-owner-session-id")
    private static GENERIC_OTHER_OWNER_SESSION_ID = new OwnerSessionId("test-owner-session-id")
    private static GENERIC_OWNER_ID = new OwnerId("test-owner-id")

    private static InitialOwnerSession genericInitialOwnerSession(OwnerSessionId ownerSessionId) {
        return new InitialOwnerSession(
            ownerSessionId,
            GENERIC_OWNER_ID
        )
    }

    private static GENERIC_PKO_SESSION_ID = new PkoSessionId("test-pko-session-id")
    private static GENERIC_FLOW_ID = new FlowId("test-flow-id")
    private static GENERIC_TOKEN = new Token("test-token")

    private static LoginInProgressOwnerSession genericLoginInProgressOwnerSession(OwnerSessionId ownerSessionId) {
        new LoginInProgressOwnerSession(
            ownerSessionId,
            GENERIC_OWNER_ID,
            GENERIC_PKO_SESSION_ID,
            GENERIC_FLOW_ID,
            GENERIC_TOKEN
        )
    }

    private static LoggedInOwnerSession genericLoggedInOwnerSession(OwnerSessionId ownerSessionId) {
        new LoggedInOwnerSession(
            ownerSessionId,
            GENERIC_OWNER_ID,
            GENERIC_PKO_SESSION_ID
        )
    }

    private static FinishedOwnerSession genericFinishedOwnerSession(OwnerSessionId ownerSessionId) {
        return new FinishedOwnerSession(
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
