package com.kontomatik.service.pko;

import com.kontomatik.lib.pko.domain.accounts.Account;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;

import java.util.List;

record AccountsResponse(
  List<AccountResponse> accounts
) {

  static AccountsResponse from(SuccessfulImport successfulImport) {
    return new AccountsResponse(
      successfulImport.accounts().list().stream()
        .map(AccountsResponse.AccountResponse::from)
        .toList()
    );
  }

  record AccountResponse(
    String name,
    String balance,
    String currency
  ) {
    static AccountsResponse.AccountResponse from(Account account) {
      return new AccountsResponse.AccountResponse(
        account.name().value(),
        account.balance().amount().value().toString(),
        account.balance().currency().value()
      );
    }
  }
}
