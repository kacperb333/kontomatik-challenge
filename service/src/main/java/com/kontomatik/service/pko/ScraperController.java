package com.kontomatik.service.pko;

import com.kontomatik.lib.pko.domain.signin.Credentials;
import com.kontomatik.lib.pko.domain.signin.Otp;
import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;
import com.kontomatik.service.pko.domain.FinishedImport.ImportId;
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
  private static final String SESSION_HEADER = "x-session";

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
  ResponseEntity<AccountsImportIdResponse> inputOtp(
    @RequestHeader(SESSION_HEADER) SessionId sessionId,
    @RequestBody OtpRequest request
  ) {
    ImportId scheduledImportId = sessionService.inputOtp(sessionId, request.otp());
    return ResponseEntity.ok(
      new AccountsImportIdResponse(scheduledImportId.value())
    );
  }

  @GetMapping("/accounts")
  ResponseEntity<?> fetchSingleImport(
    @RequestParam ImportId importId
  ) {
    return sessionService.findAccountsImport(importId)
      .map(finishedImport -> switch (finishedImport) {
        case SuccessfulImport i -> AccountsResponse.from(i);
        case FailedImport i -> FailedImportResponse.from(i);
      })
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.noContent().build());
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
}

