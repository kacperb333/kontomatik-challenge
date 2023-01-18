package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

public record AccountsImport(
  boolean isFailed,
  AccountsInfo data
) {
}
