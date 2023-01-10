package com.kontomatik.pko;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PkoScraperController {

    private final OwnerSessionService ownerSessionService;
    private final OwnerAccountsService ownerAccountsService;

    private static final String OWNER_SESSION_HEADER = "x-owner-session";

    public PkoScraperController(OwnerSessionService ownerSessionService, OwnerAccountsService ownerAccountsService) {
        this.ownerSessionService = ownerSessionService;
        this.ownerAccountsService = ownerAccountsService;
    }

    @PostMapping("/owner")
    ResponseEntity<OwnerIdResponse> initializeSession(@RequestParam OwnerId ownerId) {
        var initialSession = ownerSessionService.initializeOwnerSession(ownerId);
        return ResponseEntity.ok()
            .headers(ownerSessionId(initialSession.ownerSessionId()))
            .body(new OwnerIdResponse(initialSession.ownerId().value()));
    }

    @PostMapping("/owner/login")
    ResponseEntity<OwnerIdResponse> logIn(
        @RequestHeader(OWNER_SESSION_HEADER) OwnerSessionId ownerSessionId,
        @RequestBody CredentialsRequest request
    ) {
        var logInResult = ownerSessionService.logIn(ownerSessionId, request.credentials());
        return ResponseEntity.ok()
            .headers(ownerSessionId(logInResult.ownerSessionId()))
            .body(new OwnerIdResponse(logInResult.ownerId().value()));
    }

    @PostMapping("/owner/otp")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<OwnerIdResponse> inputOtp(
        @RequestHeader(OWNER_SESSION_HEADER) OwnerSessionId ownerSessionId,
        @RequestBody OtpRequest request
    ) {
        var inputOtpResult = ownerSessionService.inputOtp(ownerSessionId, request.otp());
        return ResponseEntity.ok()
            .headers(ownerSessionId(inputOtpResult.ownerSessionId()))
            .body(new OwnerIdResponse(inputOtpResult.ownerId().value()));
    }

    @PostMapping("/import")
    ResponseEntity<ImportAccountsResponse> importAccounts(
        @RequestHeader(OWNER_SESSION_HEADER) OwnerSessionId ownerSessionId
    ) {
        var scheduleImportResult = ownerAccountsService.scheduleFetchOwnerAccountsInfo(ownerSessionId);
        return ResponseEntity.accepted()
            .headers(ownerSessionId(scheduleImportResult.ownerSessionId()))
            .body(new ImportAccountsResponse(scheduleImportResult.accountsImportId().value()));
    }

    @GetMapping("/import")
    ResponseEntity<AccountsInfoResponse> fetchSingleImport(@RequestParam AccountsImportId accountsImportId) {
        return ownerAccountsService.fetchSingleImport(accountsImportId)
            .map(it -> new AccountsInfoResponse(it.accounts()))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/imports")
    ResponseEntity<AccountsInfoResponse> fetchAllImports(@RequestParam OwnerId ownerId) {
        return ownerAccountsService.fetchAllImportsForOwner(ownerId)
            .map(it -> new AccountsInfoResponse(it.accounts()))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static HttpHeaders ownerSessionId(OwnerSessionId ownerSessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(OWNER_SESSION_HEADER, ownerSessionId.value());
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

    private record OwnerIdResponse(
        String ownerId
    ) {
    }

    private record ImportAccountsResponse(
        String accountImportId
    ) {
    }

    private record AccountsInfoResponse(
        List<AccountInfo> accounts
    ) {
    }
}

