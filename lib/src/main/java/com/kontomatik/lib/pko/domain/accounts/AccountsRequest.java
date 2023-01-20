package com.kontomatik.lib.pko.domain.accounts;

record AccountsRequest(
  Integer version,
  AccountsRequestData data
) {
  static final int REQUEST_VERSION = 3;

  static AccountsRequest newRequest() {
    return new AccountsRequest(
      REQUEST_VERSION,
      new AccountsRequestData(
        new AccountsMarker()
      )
    );
  }

  record AccountsRequestData(
    AccountsMarker accounts
  ) {
  }

  public record AccountsMarker() {
  }
}
