package com.kontomatik.lib.pko.domain.accounts;

import java.util.Collections;
import java.util.List;

public record AccountsInfo(
  List<AccountInfo> accounts
) {
  public static final AccountsInfo EMPTY = new AccountsInfo(Collections.emptyList());
}
