package com.kontomatik.service.pko;

import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.service.pko.domain.SessionNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class PkoScraperExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(PkoScraperExceptionHandler.class);

  @ExceptionHandler
  ResponseEntity<ErrorMessage> handle(PkoScraperFacade.PkoScraperFacadeBug ex) {
    return warnAndRespond(
      ex,
      "Unexpected error occurred, please contact us.",
      HttpStatus.INTERNAL_SERVER_ERROR
    );
  }

  @ExceptionHandler(PkoScraperFacade.LoginFailed.class)
  ResponseEntity<ErrorMessage> handle(PkoScraperFacade.LoginFailed ex) {
    return warnAndRespond(
      ex,
      String.format("Login failed: %s.", ex.getMessage()),
      HttpStatus.UNPROCESSABLE_ENTITY
    );
  }

  @ExceptionHandler(SessionNotFound.class)
  ResponseEntity<ErrorMessage> handle(SessionNotFound ex) {
    return warnAndRespond(
      ex,
      String.format("Session with id [%s] not found. Make sure proper x-session header is set.", ex.sessionId.value()),
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
