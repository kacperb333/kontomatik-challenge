package com.kontomatik.service.pko;

import com.kontomatik.lib.pko.domain.accounts.Account;
import com.kontomatik.lib.pko.domain.signin.Credentials;
import com.kontomatik.lib.pko.domain.signin.Otp;
import com.kontomatik.service.pko.domain.FinishedImport;
import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;
import com.kontomatik.service.pko.domain.SessionId;
import com.kontomatik.service.pko.domain.SessionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class ScraperController {
  private final SessionService sessionService;
  static final String SESSION_HEADER = "x-session";

  ScraperController(
    SessionService sessionService
  ) {
    this.sessionService = sessionService;
  }

  @PostMapping("/session")
  ResponseEntity<Void> signIn(
    @RequestBody CredentialsRequest request
  ) {
    var logInResult = sessionService.signIn(request.credentials());
    return ResponseEntity.ok()
      .headers(sessionHeader(logInResult.sessionId()))
      .build();
  }

  @PostMapping("/session/otp")
  @ResponseStatus(HttpStatus.OK)
  ResponseEntity<Void> inputOtp(
    @RequestHeader(SESSION_HEADER) SessionId sessionId,
    @RequestBody OtpRequest request
  ) {
    var inputOtpResult = sessionService.inputOtp(sessionId, request.otp());
    return ResponseEntity.ok()
      .headers(sessionHeader(inputOtpResult.sessionId()))
      .build();
  }

  @GetMapping("/session/accounts")
  ResponseEntity<AccountsResponse> fetchSingleImport(
    @RequestHeader(SESSION_HEADER) SessionId sessionId
  ) {
    return sessionService.findSessionAccountsImport(sessionId)
      .map(AccountsResponse::from)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.ok().build());
  }

  private static HttpHeaders sessionHeader(SessionId sessionId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(SESSION_HEADER, sessionId.value());
    return headers;
  }

  private record CredentialsRequest(
    Credentials credentials
  ) {
  }

  private record OtpRequest(
    Otp otp
  ) {
  }

  record AccountsResponse(
    Object data
  ) {
    static ScraperController.AccountsResponse from(FinishedImport accountsImport) {
      return switch (accountsImport) {
        case SuccessfulImport i -> success(i);
        case FailedImport i -> error(i);
      };
    }

    static ScraperController.AccountsResponse success(SuccessfulImport successfulImport) {
      return new ScraperController.AccountsResponse(
        successfulImport.accounts().list().stream()
          .map(ScraperController.AccountsResponse.AccountResponse::from)
          .toList()
      );
    }

    static ScraperController.AccountsResponse error(FailedImport failedImport) {
      return new ScraperController.AccountsResponse(
        String.format("Import failed for session ['%s']", failedImport.sessionId().value())
      );
    }

    record AccountResponse(
      String name,
      String balance,
      String currency
    ) {
      static AccountResponse from(Account account) {
        return new AccountResponse(
          account.name().value(),
          account.balance().amount().value().toString(),
          account.balance().currency().value()
        );
      }
    }
  }
}

