package com.kontomatik.pko.service.api;

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo;
import com.kontomatik.pko.lib.usecase.login.Credentials;
import com.kontomatik.pko.lib.usecase.login.Otp;
import com.kontomatik.pko.service.domain.accounts.AccountsImport;
import com.kontomatik.pko.service.domain.accounts.AccountsImportId;
import com.kontomatik.pko.service.domain.accounts.AccountsService;
import com.kontomatik.pko.service.domain.session.SessionId;
import com.kontomatik.pko.service.domain.session.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
class PkoScraperController {
  private final SessionService sessionService;
  private final AccountsService accountsService;
  private final SessionHeaderProvider sessionHeaderProvider;

  private static final String SESSION_HEADER = "x-session";

  PkoScraperController(
    SessionService sessionService,
    AccountsService accountsService,
    SessionHeaderProvider sessionHeaderProvider
  ) {
    this.sessionService = sessionService;
    this.accountsService = accountsService;
    this.sessionHeaderProvider = sessionHeaderProvider;
  }

  @PostMapping("/session")
  ResponseEntity<Void> initializeSession() {
    var initialSession = sessionService.initializeSession();
    return ResponseEntity.ok()
      .headers(sessionHeaderProvider.sessionHeader(initialSession.SessionId()))
      .build();
  }

  @PostMapping("/session/login")
  ResponseEntity<Void> logIn(
    @RequestHeader(SESSION_HEADER) SessionId sessionId,
    @RequestBody CredentialsRequest request
  ) {
    var logInResult = sessionService.logIn(sessionId, request.credentials());
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

  @PostMapping("/import")
  ResponseEntity<ImportAccountsResponse> importAccounts(
    @RequestHeader(SESSION_HEADER) SessionId sessionId
  ) {
    var scheduleImportResult = accountsService.scheduleFetchAccountsInfo(sessionId);
    return ResponseEntity.accepted()
      .headers(sessionHeaderProvider.sessionHeader(scheduleImportResult.sessionId()))
      .body(new ImportAccountsResponse(scheduleImportResult.accountsImportId().value()));
  }

  @GetMapping("/import")
  ResponseEntity<AccountsInfoResponse> fetchSingleImport(@RequestParam AccountsImportId accountsImportId) {
    return accountsService.fetchSingleImport(accountsImportId)
      .map(it -> new AccountsInfoResponse(
        it.accountsImportId().value(),
        it.status(),
        it.accountsInfo().accounts(),
        it.details().value())
      )
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private record CredentialsRequest(
    Credentials credentials
  ) {
  }

  private record OtpRequest(
    Otp otp
  ) {
  }

  private record ImportAccountsResponse(
    String accountImportId
  ) {
  }

  private record AccountsInfoResponse(
    String accountsImportId,
    AccountsImport.Status importStatus,
    List<AccountInfo> accounts,
    String additionalMessage
  ) {
  }
}

