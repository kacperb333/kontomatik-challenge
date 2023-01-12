package com.kontomatik.pko;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PkoScraperExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(PkoScraperExceptionHandler.class);

    @ExceptionHandler(PkoClient.PkoClientIOException.class)
    ResponseEntity<ErrorMessage> handle(PkoClient.PkoClientIOException ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorMessage(
                "BANKING_SERVICE_UNAVAILABLE",
                "Problems with PKO banking service, try again later"
            ));
    }

    @ExceptionHandler(UnexpectedAction.class)
    ResponseEntity<ErrorMessage> handle(UnexpectedAction ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorMessage(
                "UNEXPECTED_ACTION",
                "Unexpected action in login flow, please login to your banking system manually in order to clear any required actions during login (e.g. popup notification windows)"
            ));
    }

    @ExceptionHandler(OwnerSessionNotInitialized.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionNotInitialized ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorMessage(
                "OWNER_SESSION_NOT_INITIALIZED",
                "Owner session has not been initialized. Make sure proper x-owner-session header is set"
            ));
    }

    @ExceptionHandler(OwnerSessionLoginNotInProgress.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionLoginNotInProgress ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorMessage(
                "OWNER_SESSION_LOGIN_NOT_IN_PROGRESS",
                "Owner session has no login in progress. Make sure proper x-owner-session header is set"
            ));
    }

    @ExceptionHandler(OwnerSessionNotLoggedIn.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionNotLoggedIn ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorMessage(
                "OWNER_SESSION_NOT_LOGGED_IN",
                "Owner session is not logged in to banking system. Make sure proper x-owner-session header is set"
            ));
    }

    record ErrorMessage(
        String code,
        String message
    ) {
    }
}
