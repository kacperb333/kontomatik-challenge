package com.kontomatik.pko.service.domain.accounts;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

import java.time.Instant;

public record AccountsImport(
  AccountsImportId accountsImportId,
  Status status,
  Instant createdAt,
  AccountsInfo accountsInfo,
  Details details
) {
  static AccountsImport inProgress(
    AccountsImportId accountsImportId,
    Instant createdAt
  ) {
    return new AccountsImport(
      accountsImportId,
      Status.IN_PROGRESS,
      createdAt,
      AccountsInfo.EMPTY,
      Details.EMPTY
    );
  }

  static AccountsImport success(
    AccountsImportId accountsImportId,
    AccountsInfo accountsInfo,
    Instant createdAt
  ) {
    return new AccountsImport(
      accountsImportId,
      Status.SUCCESS,
      createdAt,
      accountsInfo,
      Details.EMPTY
    );
  }

  static AccountsImport failure(
    AccountsImportId accountsImportId,
    Instant createdAt,
    Details details
  ) {
    return new AccountsImport(
      accountsImportId,
      Status.FAILED,
      createdAt,
      AccountsInfo.EMPTY,
      details
    );
  }

  public enum Status {
    IN_PROGRESS, SUCCESS, FAILED
  }

  public record Details(
    String value
  ) {
    public static final Details EMPTY = new Details(null);

    static Details ofMessage(String message) {
      return new Details(message);
    }
  }
}
