package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.PkoClient;
import com.kontomatik.pko.domain.UnexpectedAction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//TODO add logging
@ControllerAdvice
public class PkoScraperExceptionHandler {

    @ExceptionHandler(PkoClient.PkoClientIOException.class)
    ResponseEntity<ErrorMessage> handle(PkoClient.PkoClientIOException ex) {
        System.out.println(ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorMessage(
                "BANKING_SERVICE_UNAVAILABLE",
                "Problems with PKO banking service, try again later"
            ));
    }

    @ExceptionHandler(UnexpectedAction.class)
    ResponseEntity<ErrorMessage> handle(UnexpectedAction ex) {
        System.out.println(ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorMessage(
                "UNEXPECTED_ACTION",
                "Unexpected action in login flow, please login to your banking system manually in order to clear any required actions during login (e.g. popup notification windows)"
            ));
    }

    @ExceptionHandler(OwnerSessionNotInitialized.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionNotInitialized ex) {
        System.out.println(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorMessage(
                "OWNER_SESSION_NOT_INITIALIZED",
                "Owner session has not been initialized. Make sure proper x-owner-session header is set"
            ));
    }

    @ExceptionHandler(OwnerSessionLoginNotInProgress.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionLoginNotInProgress ex) {
        System.out.println(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorMessage(
                "OWNER_SESSION_LOGIN_NOT_IN_PROGRESS",
                "Owner session has no login in progress. Make sure proper x-owner-session header is set"
            ));
    }

    @ExceptionHandler(OwnerSessionNotLoggedIn.class)
    ResponseEntity<ErrorMessage> handle(OwnerSessionNotLoggedIn ex) {
        System.out.println(ex.getMessage());
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
