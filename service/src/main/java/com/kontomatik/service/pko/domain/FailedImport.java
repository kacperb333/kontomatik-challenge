package com.kontomatik.service.pko.domain;

public record FailedImport(
  SessionId sessionId
) implements FinishedImport {
}
