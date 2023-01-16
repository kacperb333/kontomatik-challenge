package com.kontomatik.pko.service.domain.accounts;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

import java.time.Instant;

public record AccountsImport(
  AccountsImportId accountsImportId,
  Status status,
  Instant createdAt,
  AccountsInfo accountsInfo
) {
  static AccountsImport success(
    AccountsImportId accountsImportId,
    AccountsInfo accountsInfo,
    Instant createdAt
  ) {
    return new AccountsImport(
      accountsImportId,
      Status.SUCCESS,
      createdAt,
      accountsInfo
    );
  }

  static AccountsImport failure(
    AccountsImportId accountsImportId,
    Instant createdAt
  ) {
    return new AccountsImport(
      accountsImportId,
      Status.FAILED,
      createdAt,
      AccountsInfo.EMPTY
    );
  }

  public enum Status {
    IN_PROGRESS, SUCCESS, FAILED
  }
}
