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

    @ExceptionHandler(OwnerSessionNotInitialized.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionNotInitialized ex) {
        return warnAndRespond(
            ex,
            "Owner session has not been initialized. Make sure proper x-owner-session header is set",
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(OwnerSessionLoginNotInProgress.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionLoginNotInProgress ex) {
        return warnAndRespond(
            ex,
            "Owner session has no login in progress. Make sure proper x-owner-session header is set",
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(OwnerSessionNotLoggedIn.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionNotLoggedIn ex) {
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
