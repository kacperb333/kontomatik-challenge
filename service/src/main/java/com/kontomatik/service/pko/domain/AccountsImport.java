package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.AccountsInfo;

public record AccountsImport(
  boolean isFailed,
  AccountsInfo data
) {
}
