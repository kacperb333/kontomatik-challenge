package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.service.pko.domain.FinishedImport.ImportId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringPersistentAccountsImportRepository extends MongoRepository<PersistentImport, ImportId> {
}
