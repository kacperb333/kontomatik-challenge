package com.kontomatik.service.pko;

import com.kontomatik.lib.pko.domain.accounts.Account;
import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.login.Credentials;
import com.kontomatik.lib.pko.domain.login.Otp;
import com.kontomatik.service.pko.domain.AccountsImport;
import com.kontomatik.service.pko.domain.SessionId;
import com.kontomatik.service.pko.domain.SessionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
class PkoScraperController {
  private final SessionService sessionService;
  static final String SESSION_HEADER = "x-session";

  PkoScraperController(
    SessionService sessionService
  ) {
    this.sessionService = sessionService;
  }

  @PostMapping("/session")
  ResponseEntity<Void> logIn(
    @RequestBody CredentialsRequest request
  ) {
    var logInResult = sessionService.logIn(request.credentials());
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
    AccountsImport importedAccounts = sessionService.getSessionAccountsImport(sessionId);
    return ResponseEntity.ok(
      AccountsResponse.from(importedAccounts.data(), importedAccounts.isFailed())
    );
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

  private record AccountsResponse(
    boolean isFailed,
    List<AccountResponse> accounts
  ) {
    static AccountsResponse from(Accounts accounts, boolean isFailed) {
      return new AccountsResponse(
        isFailed,
        accounts.list().stream()
          .map(AccountResponse::from)
          .toList()
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
          account.balance().amount().value(),
          account.balance().currency().value()
        );
      }

    }
  }
}

