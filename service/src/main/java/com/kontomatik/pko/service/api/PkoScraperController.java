package com.kontomatik.pko.service.api;

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo;
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.lib.usecase.login.Credentials;
import com.kontomatik.pko.lib.usecase.login.Otp;
import com.kontomatik.pko.service.domain.SessionId;
import com.kontomatik.pko.service.domain.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
class PkoScraperController {
  private final SessionService sessionService;
  private final SessionHeaderProvider sessionHeaderProvider;
  private static final String SESSION_HEADER = "x-session";

  PkoScraperController(
    SessionService sessionService,
    SessionHeaderProvider sessionHeaderProvider
  ) {
    this.sessionService = sessionService;
    this.sessionHeaderProvider = sessionHeaderProvider;
  }

  @PostMapping("/session")
  ResponseEntity<Void> logIn(
    @RequestBody CredentialsRequest request
  ) {
    var logInResult = sessionService.logIn(request.credentials());
    return ResponseEntity.ok()
      .headers(sessionHeaderProvider.sessionHeader(logInResult.sessionId()))
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
      .headers(sessionHeaderProvider.sessionHeader(inputOtpResult.sessionId()))
      .build();
  }

  @GetMapping("/session/accounts")
  ResponseEntity<AccountsInfoResponse> fetchSingleImport(
    @RequestHeader(SESSION_HEADER) SessionId sessionId
  ) {
    AccountsInfo importedAccountsInfo = sessionService.findSingleImport(sessionId);
    return ResponseEntity.ok(
      new AccountsInfoResponse(importedAccountsInfo.accounts())
    );
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

