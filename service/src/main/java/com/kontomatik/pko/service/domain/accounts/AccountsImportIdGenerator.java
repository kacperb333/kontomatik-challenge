package com.kontomatik.pko.service.domain.accounts;

import java.util.UUID;

class AccountsImportIdGenerator {
  static AccountsImportId generate() {
    return new AccountsImportId(UUID.randomUUID().toString());
  }
}
