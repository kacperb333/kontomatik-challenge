package com.kontomatik.pko.service.api;

import com.kontomatik.pko.lib.client.PkoClient;
import com.kontomatik.pko.lib.usecase.login.UnexpectedAction;
import com.kontomatik.pko.service.domain.session.FinishedSession;
import com.kontomatik.pko.service.domain.session.SessionNotInitialized;
import com.kontomatik.pko.service.domain.session.SessionNotLoggedIn;
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

  @ExceptionHandler(SessionNotInitialized.class)
  ResponseEntity<ErrorMessage> handle(SessionNotInitialized ex) {
    return warnAndRespond(
      ex,
      "Owner session has not been initialized. Make sure proper x-owner-session header is set",
      HttpStatus.UNPROCESSABLE_ENTITY
    );
  }

  @ExceptionHandler(FinishedSession.SessionLoginNotInProgress.class)
  ResponseEntity<ErrorMessage> handle(FinishedSession.SessionLoginNotInProgress ex) {
    return warnAndRespond(
      ex,
      "Owner session has no login in progress. Make sure proper x-owner-session header is set",
      HttpStatus.UNPROCESSABLE_ENTITY
    );
  }

  @ExceptionHandler(SessionNotLoggedIn.class)
  ResponseEntity<ErrorMessage> handle(SessionNotLoggedIn ex) {
    return warnAndRespond(
      ex,
      "Owner session is not logged in to banking system. Make sure proper x-owner-session header is set",
      HttpStatus.UNPROCESSABLE_ENTITY
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
