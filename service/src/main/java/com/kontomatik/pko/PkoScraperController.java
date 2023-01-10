package com.kontomatik.pko;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PkoScraperController {

    private final OwnerSessionService ownerSessionService;
    private final OwnerAccountsService ownerAccountsService;

    private final OwnerSessionHeaderProvider ownerSessionHeaderProvider;

    private static final String OWNER_SESSION_HEADER = "x-owner-session";

    public PkoScraperController(
        OwnerSessionService ownerSessionService,
        OwnerAccountsService ownerAccountsService,
        OwnerSessionHeaderProvider ownerSessionHeaderProvider
    ) {
        this.ownerSessionService = ownerSessionService;
        this.ownerAccountsService = ownerAccountsService;
        this.ownerSessionHeaderProvider = ownerSessionHeaderProvider;
    }

    @PostMapping("/owner")
    ResponseEntity<OwnerIdResponse> initializeSession(@RequestParam OwnerId ownerId) {
        var initialSession = ownerSessionService.initializeOwnerSession(ownerId);
        return ResponseEntity.ok()
            .headers(ownerSessionHeaderProvider.ownerSessionHeader(initialSession.ownerSessionId()))
            .body(new OwnerIdResponse(initialSession.ownerId().value()));
    }

    @PostMapping("/owner/login")
    ResponseEntity<OwnerIdResponse> logIn(
        @RequestHeader(OWNER_SESSION_HEADER) OwnerSessionId ownerSessionId,
        @RequestBody CredentialsRequest request
    ) {
        var logInResult = ownerSessionService.logIn(ownerSessionId, request.credentials());
        return ResponseEntity.ok()
            .headers(ownerSessionHeaderProvider.ownerSessionHeader(logInResult.ownerSessionId()))
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
            .headers(ownerSessionHeaderProvider.ownerSessionHeader(inputOtpResult.ownerSessionId()))
            .body(new OwnerIdResponse(inputOtpResult.ownerId().value()));
    }

    @PostMapping("/import")
    ResponseEntity<ImportAccountsResponse> importAccounts(
        @RequestHeader(OWNER_SESSION_HEADER) OwnerSessionId ownerSessionId
    ) {
        var scheduleImportResult = ownerAccountsService.scheduleFetchOwnerAccountsInfo(ownerSessionId);
        return ResponseEntity.accepted()
            .headers(ownerSessionHeaderProvider.ownerSessionHeader(scheduleImportResult.ownerSessionId()))
            .body(new ImportAccountsResponse(scheduleImportResult.accountsImportId().value()));
    }

    @GetMapping("/import")
    ResponseEntity<AccountsInfoResponse> fetchSingleImport(@RequestParam AccountsImportId accountsImportId) {
        return ownerAccountsService.fetchSingleImport(accountsImportId)
            .map(it -> new AccountsInfoResponse(
                it.accountsImportId().value(),
                it.status(),
                it.accountsInfo().accounts())
            )
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/imports")
    ResponseEntity<List<AccountsInfoResponse>> fetchAllImports(@RequestParam OwnerId ownerId) {
        var response = ownerAccountsService.fetchAllImportsForOwner(ownerId).stream()
            .map(it -> new AccountsInfoResponse(
                it.accountsImportId().value(),
                it.status(),
                it.accountsInfo().accounts())
            )
            .toList();

        return ResponseEntity.ok(response);
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
        String accountsImportId,
        AccountsImport.Status importStatus,
        List<AccountInfo> accounts
    ) {
    }
}

