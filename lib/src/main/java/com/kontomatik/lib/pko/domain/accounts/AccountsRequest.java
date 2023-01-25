package com.kontomatik.lib.pko.domain.accounts;


import static com.kontomatik.lib.pko.domain.accounts.AccountsRequest.AccountsRequestData.ACCOUNTS_MARKER;

record AccountsRequest(
  Integer version,
  AccountsRequestData data
) {
  static final int REQUEST_VERSION = 3;

  static AccountsRequest newRequest() {
    return new AccountsRequest(
      REQUEST_VERSION,
      ACCOUNTS_MARKER
    );
  }

  record AccountsRequestData(
    Object accounts
  ) {
    static AccountsRequestData ACCOUNTS_MARKER = new AccountsRequestData(new Object());
  }
}
