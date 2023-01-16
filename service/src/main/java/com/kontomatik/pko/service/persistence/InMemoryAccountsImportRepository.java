package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.accounts.AccountsImport;
import com.kontomatik.pko.service.domain.accounts.AccountsImportId;
import com.kontomatik.pko.service.domain.accounts.AccountsImportRepository;
import com.kontomatik.pko.service.domain.session.OwnerId;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemoryAccountsImportRepository implements AccountsImportRepository {

  private final ConcurrentMap<AccountsImportId, AccountsImport> imports = new ConcurrentHashMap<>();

  @Override
  public AccountsImport store(AccountsImport accountsImport) {
    imports.put(accountsImport.accountsImportId(), accountsImport);
    return accountsImport;
  }

  @Override
  public Optional<AccountsImport> fetchOneNewerThan(AccountsImportId accountsImportId, Instant maxTime) {
    return Optional.ofNullable(imports.get(accountsImportId))
      .filter(it -> it.createdAt().isAfter(maxTime));
  }

  @Override
  public List<AccountsImport> fetchAllForOwnerNewerThan(OwnerId ownerId, Instant maxTime) {
    return imports.values().stream()
      .filter(it -> it.ownerId().equals(ownerId))
      .filter(it -> it.createdAt().isAfter(maxTime))
      .toList();
  }
}
