package com.kontomatik.pko.persistence;

import com.kontomatik.pko.service.AccountsImport;
import com.kontomatik.pko.service.AccountsImportId;
import com.kontomatik.pko.service.AccountsImportRepository;
import com.kontomatik.pko.service.OwnerId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryAccountsImportRepository implements AccountsImportRepository {

    private final ConcurrentMap<AccountsImportId, AccountsImport> imports = new ConcurrentHashMap<>();

    @Override
    public AccountsImport store(AccountsImport accountsImport) {
        imports.put(accountsImport.accountsImportId(), accountsImport);
        return accountsImport;
    }

    @Override
    public Optional<AccountsImport> fetch(AccountsImportId accountsImportId) {
        return Optional.ofNullable(imports.get(accountsImportId));
    }

    @Override
    public List<AccountsImport> fetchAll(OwnerId ownerId) {
        return imports.values().stream()
            .filter(it -> it.ownerId().equals(ownerId))
            .toList();
    }
}
