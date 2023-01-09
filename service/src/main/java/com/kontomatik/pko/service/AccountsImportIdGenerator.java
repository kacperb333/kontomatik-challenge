package com.kontomatik.pko.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountsImportIdGenerator {
    AccountsImportId generate() {
        return new AccountsImportId(UUID.randomUUID().toString());
    }
}
