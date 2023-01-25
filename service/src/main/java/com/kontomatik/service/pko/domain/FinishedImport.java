package com.kontomatik.service.pko.domain;

public sealed interface FinishedImport permits FailedImport, SuccessfulImport {
}
