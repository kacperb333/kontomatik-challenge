package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.service.pko.domain.SessionId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringPersistentAccountsImportRepository extends MongoRepository<PersistentImport, SessionId> {
}
