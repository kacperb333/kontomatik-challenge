package com.kontomatik.pko.service.persistence

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo
import com.kontomatik.pko.service.domain.accounts.AccountsImport
import com.kontomatik.pko.service.domain.accounts.AccountsImportId
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant
import java.time.ZonedDateTime

class InMemoryAccountsImportRepositorySpec extends Specification {

  @Subject
  InMemoryAccountsImportRepository repository = new InMemoryAccountsImportRepository()

  private static SOME_INSTANT = instant("2020-01-01T10:00:00+01:00")

  def "should fetch single stored import by id"() {
    given:
    Instant importCreatedTime = SOME_INSTANT

    and:
    AccountsImport anImport = new AccountsImport(
      new AccountsImportId("test-import-id"),
      AccountsImport.Status.SUCCESS,
      importCreatedTime,
      new AccountsInfo([
        new AccountInfo("test-account", "1221.21", "PLN")
      ])
    )
    AccountsImport differentImport = new AccountsImport(
      new AccountsImportId("different-import-id"),
      AccountsImport.Status.SUCCESS,
      importCreatedTime,
      new AccountsInfo([
        new AccountInfo("different-account", "2221.21", "EUR")
      ])
    )

    and:
    repository.save(anImport)
    repository.save(differentImport)

    when:
    AccountsImport fetchedImport = repository.findOne(
      new AccountsImportId("test-import-id")
    ).get()

    then:
    with(fetchedImport) {
      it.accountsImportId() == new AccountsImportId("test-import-id")
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

  def "should not fetch single nonexistent import"() {
    given:
    Instant someInstant = SOME_INSTANT

    and:
    repository.save(genericImport(
      new AccountsImportId("test-import-1"),
      someInstant
    ))
    repository.save(genericImport(
      new AccountsImportId("test-import-2"),
      someInstant.plusMillis(1000)
    ))
    repository.save(genericImport(
      new AccountsImportId("test-import-3"),
      someInstant.plusMillis(2000)
    ))

    expect:
    repository
      .findOne(new AccountsImportId("nonexistent"))
      .isEmpty()
  }

  private static Instant instant(String dateTime) {
    return ZonedDateTime.parse(dateTime).toInstant()
  }

  private static AccountsImport genericImport(AccountsImportId accountsImportId, Instant createdAt) {
    return new AccountsImport(
      accountsImportId,
      AccountsImport.Status.SUCCESS,
      createdAt,
      new AccountsInfo([
        new AccountInfo("some-account", "1337.69", "PLN")
      ])
    )
  }
}
