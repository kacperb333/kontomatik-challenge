package com.kontomatik.pko.service;

import com.kontomatik.pko.lib.client.PkoClient;
import com.kontomatik.pko.lib.usecase.login.UnexpectedAction;
import com.kontomatik.pko.service.domain.SessionNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class PkoScraperExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(PkoScraperExceptionHandler.class);

  @ExceptionHandler(PkoClient.PkoClientIOException.class)
  ResponseEntity<ErrorMessage> handle(PkoClient.PkoClientIOException ex) {
    return warnAndRespond(
      ex,
      "Problems with PKO banking service, try again later",
      HttpStatus.SERVICE_UNAVAILABLE
    );
  }

  @ExceptionHandler(UnexpectedAction.class)
  ResponseEntity<ErrorMessage> handle(UnexpectedAction ex) {
    return warnAndRespond(
      ex,
      "Unexpected action in login flow, please login to your banking system manually in order to clear any required actions during login (e.g. popup notification windows)",
      HttpStatus.SERVICE_UNAVAILABLE
    );
  }

  @ExceptionHandler(SessionNotFound.class)
  ResponseEntity<ErrorMessage> handle(SessionNotFound ex) {
    return warnAndRespond(
      ex,
      String.format("Session with id [%s] not found. Make sure proper x-session header is set", ex.sessionId.value()),
      HttpStatus.NOT_FOUND
    );
  }

  private ResponseEntity<ErrorMessage> warnAndRespond(Exception ex, String humanReadableMessage, HttpStatus httpStatus) {
    log.warn("Unrecoverable exception encountered", ex);
    return ResponseEntity.status(httpStatus)
      .body(new ErrorMessage(
        ex.getClass().getSimpleName(),
        humanReadableMessage
      ));
  }

  record ErrorMessage(
    String code,
    String message
  ) {
  }
}
