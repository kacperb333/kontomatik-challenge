package com.kontomatik.lib.pko.domain.accounts;

import java.util.Collections;
import java.util.List;

public record Accounts(
  List<Account> list
) {
  public static final Accounts EMPTY = new Accounts(Collections.emptyList());
}
