package com.kontomatik.service.pko.persistence.accounts;

import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;
import com.kontomatik.service.pko.domain.FinishedImport.ImportId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.TypeAlias;

import java.time.Instant;

@TypeAlias("PersistentFailedImport")
class PersistentFailedImport extends PersistentImport {

  @PersistenceCreator
  public PersistentFailedImport(
    ImportId importId,
    Instant persistedAt
  ) {
    super(
      importId,
      persistedAt
    );
  }

  static PersistentFailedImport fromDomain(FailedImport domainImport, Instant at) {
    return new PersistentFailedImport(
      domainImport.importId(),
      at
    );
  }

  @Override
  FailedImport toDomain() {
    return new FailedImport(importId);
  }
}
