package com.kontomatik.service.pko;

import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;

record FailedImportResponse(
  String message
) {
  static FailedImportResponse from(FailedImport failedImport) {
    return new FailedImportResponse(
      String.format("Import ['%s'] failed.", failedImport.importId().value())
    );
  }
}

