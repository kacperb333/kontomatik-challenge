package com.kontomatik.pko


import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant
import java.time.ZonedDateTime

class InMemoryAccountsImportRepositorySpec extends Specification {

    @Subject
    InMemoryAccountsImportRepository repository = new InMemoryAccountsImportRepository()

    private static SOME_INSTANT = instant("2020-01-01T10:00:00+01:00")

    def "should fetch single stored import by id when it's older than given instant"() {
        given:
        Instant importCreatedTime = SOME_INSTANT

        and:
        AccountsImport anImport = new AccountsImport(
            new AccountsImportId("test-import-id"),
            new OwnerId("test-owner-id"),
            AccountsImport.Status.SUCCESS,
            importCreatedTime,
            new AccountsInfo([
                new AccountInfo("test-account", "1221.21", "PLN")
            ]),
            AccountsImport.Details.EMPTY
        )
        AccountsImport differentImport = new AccountsImport(
            new AccountsImportId("different-import-id"),
            new OwnerId("test-owner-id"),
            AccountsImport.Status.SUCCESS,
            importCreatedTime,
            new AccountsInfo([
                new AccountInfo("different-account", "2221.21", "EUR")
            ]),
            AccountsImport.Details.EMPTY
        )

        and:
        repository.store(anImport)
        repository.store(differentImport)

        when:
        AccountsImport fetchedImport = repository.fetchOneNewerThan(
            new AccountsImportId("test-import-id"),
            importCreatedTime.minusMillis(1)
        ).get()

        then:
        with(fetchedImport) {
            it.accountsImportId() == new AccountsImportId("test-import-id")
            it.ownerId() == new OwnerId("test-owner-id")
            it.status() == AccountsImport.Status.SUCCESS
            it.createdAt() == SOME_INSTANT
            with(it.accountsInfo()) {
                it.accounts().size() == 1
                with(it.accounts().find { it.name() == "test-account" }) {
                    it.balance() == "1221.21"
                    it.currency() == "PLN"
                }
            }
        }
    }

    def "should fetch all stored imports by owner id older than given instant"() {
        given:
        Instant importCreatedTime = SOME_INSTANT

        and:
        AccountsImport import1 = new AccountsImport(
            new AccountsImportId("test-import-id-1"),
            new OwnerId("test-owner-id"),
            AccountsImport.Status.SUCCESS,
            importCreatedTime,
            new AccountsInfo([
                new AccountInfo("test-account-1", "1221.21", "PLN")
            ]),
            AccountsImport.Details.EMPTY
        )
        AccountsImport import2 = new AccountsImport(
            new AccountsImportId("test-import-id-2"),
            new OwnerId("test-owner-id"),
            AccountsImport.Status.SUCCESS,
            importCreatedTime,
            new AccountsInfo([
                new AccountInfo("test-account-2", "2221.21", "EUR"),
                new AccountInfo("test-account-3", "3221.21", "USD")
            ]),
            AccountsImport.Details.EMPTY
        )
        AccountsImport importOfDifferentOwner = new AccountsImport(
            new AccountsImportId("test-import-id-3"),
            new OwnerId("different-owner-id"),
            AccountsImport.Status.SUCCESS,
            importCreatedTime,
            new AccountsInfo([
                new AccountInfo("different-account-1", "2221.21", "EUR"),
                new AccountInfo("different-account-2", "3221.21", "USD")
            ]),
            AccountsImport.Details.EMPTY
        )

        and:
        repository.store(import1)
        repository.store(import2)
        repository.store(importOfDifferentOwner)

        when:
        List<AccountsImport> fetchedImports = repository.fetchAllForOwnerNewerThan(
            new OwnerId("test-owner-id"),
            importCreatedTime.minusMillis(1)
        )

        then:
        with(fetchedImports) {
            it.size() == 2
            with(it.find { it.accountsImportId() == new AccountsImportId("test-import-id-1") }) {
                it.ownerId() == new OwnerId("test-owner-id")
                it.status() == AccountsImport.Status.SUCCESS
                it.createdAt() == SOME_INSTANT
                with(it.accountsInfo()) {
                    it.accounts().size() == 1
                    with(it.accounts().find { it.name() == "test-account-1" }) {
                        it.balance() == "1221.21"
                        it.currency() == "PLN"
                    }
                }
            }
            with(it.find { it.accountsImportId() == new AccountsImportId("test-import-id-2") }) {
                it.ownerId() == new OwnerId("test-owner-id")
                it.status() == AccountsImport.Status.SUCCESS
                it.createdAt() == SOME_INSTANT
                with(it.accountsInfo()) {
                    it.accounts().size() == 2
                    with(it.accounts().find { it.name() == "test-account-2" }) {
                        it.balance() == "2221.21"
                        it.currency() == "EUR"
                    }
                    with(it.accounts().find { it.name() == "test-account-3" }) {
                        it.balance() == "3221.21"
                        it.currency() == "USD"
                    }
                }
            }
        }
    }

    def "should skip fetching single import if it's older than given instant"() {
        given:
        Instant importCreatedTime = SOME_INSTANT

        and:
        repository.store(genericImport(
            new AccountsImportId("test-import"),
            new OwnerId("test-owner"),
            importCreatedTime
        ))

        when:
        Optional<AccountsImport> fetchedImport = repository.fetchOneNewerThan(
            new AccountsImportId("test-import"),
            testedInstant
        )

        then:
        fetchedImport.isPresent() == importFetched

        where:
        testedInstant               || importFetched
        SOME_INSTANT.minusMillis(1) || true
        SOME_INSTANT                || false
        SOME_INSTANT.plusMillis(1)  || false
    }

    def "should skip fetching imports of an owner that are older than given instant"() {
        given:
        Instant someInstant = SOME_INSTANT

        and:
        repository.store(genericImport(
            new AccountsImportId("test-import-1"),
            new OwnerId("test-owner"),
            someInstant
        ))
        repository.store(genericImport(
            new AccountsImportId("test-import-2"),
            new OwnerId("test-owner"),
            someInstant.plusMillis(1000)
        ))
        repository.store(genericImport(
            new AccountsImportId("test-import-3"),
            new OwnerId("test-owner"),
            someInstant.plusMillis(2000)
        ))

        when:
        List<String> fetchedImports = repository.fetchAllForOwnerNewerThan(new OwnerId("test-owner"), testedInstant)
            *.accountsImportId()
            *.value()

        then:
        fetchedImports.toSet() == expectedFetchedImports.toSet()

        where:
        testedInstant                 || expectedFetchedImports
        SOME_INSTANT.minusMillis(1)   || ["test-import-1", "test-import-2", "test-import-3"]
        SOME_INSTANT                  || ["test-import-2", "test-import-3"]
        SOME_INSTANT.plusMillis(999)  || ["test-import-2", "test-import-3"]
        SOME_INSTANT.plusMillis(1000) || ["test-import-3"]
        SOME_INSTANT.plusMillis(1999) || ["test-import-3"]
        SOME_INSTANT.plusMillis(2000) || []
    }

    def "should not fetch single nonexistent import"() {
        given:
        Instant someInstant = SOME_INSTANT

        and:
        repository.store(genericImport(
            new AccountsImportId("test-import-1"),
            new OwnerId("test-owner"),
            someInstant
        ))
        repository.store(genericImport(
            new AccountsImportId("test-import-2"),
            new OwnerId("test-owner"),
            someInstant.plusMillis(1000)
        ))
        repository.store(genericImport(
            new AccountsImportId("test-import-3"),
            new OwnerId("test-owner"),
            someInstant.plusMillis(2000)
        ))

        expect:
        repository
            .fetchOneNewerThan(new AccountsImportId("nonexistent"), SOME_INSTANT.minusMillis(1))
            .isEmpty()
    }

    def "should not fetch imports for nonexistent owner"() {
        given:
        Instant someInstant = SOME_INSTANT

        and:
        repository.store(genericImport(
            new AccountsImportId("test-import-1"),
            new OwnerId("test-owner"),
            someInstant
        ))
        repository.store(genericImport(
            new AccountsImportId("test-import-2"),
            new OwnerId("test-owner"),
            someInstant.plusMillis(1000)
        ))
        repository.store(genericImport(
            new AccountsImportId("test-import-3"),
            new OwnerId("test-owner"),
            someInstant.plusMillis(2000)
        ))

        expect:
        repository
            .fetchAllForOwnerNewerThan(new OwnerId("nonexistent"), SOME_INSTANT.minusMillis(1))
            .isEmpty()
    }

    private static Instant instant(String dateTime) {
        return ZonedDateTime.parse(dateTime).toInstant()
    }

    private static AccountsImport genericImport(AccountsImportId accountsImportId, OwnerId ownerId, Instant createdAt) {
        return new AccountsImport(
            accountsImportId,
            ownerId,
            AccountsImport.Status.SUCCESS,
            createdAt,
            new AccountsInfo([
                new AccountInfo("some-account", "1337.69", "PLN")
            ]),
            AccountsImport.Details.EMPTY
        )
    }
}
