package com.kontomatik.service.pko;

import com.kontomatik.lib.pko.domain.accounts.AccountInfo;
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
  ResponseEntity<AccountsInfoResponse> fetchSingleImport(
    @RequestHeader(SESSION_HEADER) SessionId sessionId
  ) {
    AccountsImport importedAccountsInfo = sessionService.getSessionAccountsImport(sessionId);
    return ResponseEntity.ok(
      new AccountsInfoResponse(importedAccountsInfo.isFailed(), importedAccountsInfo.data().accounts())
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

  private record AccountsInfoResponse(
    boolean isFailed,
    List<AccountInfo> accounts
  ) {
  }
}

