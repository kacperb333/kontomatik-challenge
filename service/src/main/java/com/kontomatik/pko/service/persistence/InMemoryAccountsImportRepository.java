package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.accounts.AccountsImport;
import com.kontomatik.pko.service.domain.accounts.AccountsImportId;
import com.kontomatik.pko.service.domain.accounts.AccountsImportRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemoryAccountsImportRepository implements AccountsImportRepository {

  private final ConcurrentMap<AccountsImportId, AccountsImport> imports = new ConcurrentHashMap<>();

  @Override
  public AccountsImport save(AccountsImport accountsImport) {
    imports.put(accountsImport.accountsImportId(), accountsImport);
    return accountsImport;
  }

  @Override
  public Optional<AccountsImport> findOne(AccountsImportId accountsImportId) {
    return Optional.ofNullable(imports.get(accountsImportId));
  }
}