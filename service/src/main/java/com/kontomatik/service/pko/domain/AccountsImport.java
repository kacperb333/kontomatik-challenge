package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;

public record AccountsImport(
  boolean isFailed,
  Accounts data
) {
}
