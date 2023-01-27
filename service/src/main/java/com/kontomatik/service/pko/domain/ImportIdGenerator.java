package com.kontomatik.service.pko.domain;

import com.kontomatik.service.pko.domain.FinishedImport.ImportId;

import java.util.UUID;

class ImportIdGenerator {
  static ImportId generate() {
    return new ImportId(UUID.randomUUID().toString());
  }
}
