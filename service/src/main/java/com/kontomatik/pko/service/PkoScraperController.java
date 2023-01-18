package com.kontomatik.pko.service;

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo;
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.lib.usecase.login.Credentials;
import com.kontomatik.pko.lib.usecase.login.Otp;
import com.kontomatik.pko.service.domain.SessionId;
import com.kontomatik.pko.service.domain.SessionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
class PkoScraperController {
  private final SessionService sessionService;
  private static final String SESSION_HEADER = "x-session";

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
    AccountsInfo importedAccountsInfo = sessionService.getSessionAccountsInfo(sessionId);
    return ResponseEntity.ok(
      new AccountsInfoResponse(importedAccountsInfo.accounts())
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
    List<AccountInfo> accounts
  ) {
  }
}

