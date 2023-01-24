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

  @ExceptionHandler(PkoScraperFacade.LoginFailed.class)
  ResponseEntity<ErrorMessage> handle(PkoScraperFacade.LoginFailed ex) {
    return warnAndRespond(
      "LoginFailed",
      String.format("Login failed: %s.", ex.getMessage()),
      HttpStatus.UNPROCESSABLE_ENTITY,
      ex
    );
  }

  @ExceptionHandler(SessionNotFound.class)
  ResponseEntity<ErrorMessage> handle(SessionNotFound ex) {
    return warnAndRespond(
      "SessionNotFound",
      String.format("Session with id [%s] not found. Make sure proper x-session header is set.", ex.sessionId.value()),
      HttpStatus.NOT_FOUND,
      ex
    );
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorMessage> handle(Exception ex) {
    return warnAndRespond(
      "Error",
      "Unexpected error occurred, please contact us.",
      HttpStatus.INTERNAL_SERVER_ERROR,
      ex
    );
  }

  private ResponseEntity<ErrorMessage> warnAndRespond(String code, String humanReadableMessage, HttpStatus httpStatus, Exception ex) {
    log.warn("Unrecoverable exception encountered", ex);
    return ResponseEntity.status(httpStatus)
      .body(new ErrorMessage(
        code,
        humanReadableMessage
      ));
  }

  record ErrorMessage(
    String code,
    String message
  ) {
  }
}
