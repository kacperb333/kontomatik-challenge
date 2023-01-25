package com.kontomatik.lib.pko.domain.accounts;

import java.util.List;

public record Accounts(
  List<Account> list
) {
  public static final Accounts EMPTY = new Accounts(null);
}
