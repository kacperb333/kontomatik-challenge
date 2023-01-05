package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.AccountInfo;
import com.kontomatik.pko.domain.AccountsInfo;
import com.kontomatik.pko.domain.Credentials;
import com.kontomatik.pko.domain.Otp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
class PkoScraperController {

    private final OwnerSessionService ownerSessionService;
    private final OwnerAccountsService ownerAccountsService;

    public PkoScraperController(OwnerSessionService ownerSessionService, OwnerAccountsService ownerAccountsService) {
        this.ownerSessionService = ownerSessionService;
        this.ownerAccountsService = ownerAccountsService;
    }

    @GetMapping("/hello")
    String hello() {
        return "Hello";
    }

    @PostMapping("/owner")
    @ResponseStatus(HttpStatus.CREATED)
    void initializeSession(@RequestParam OwnerId ownerId) {
        ownerSessionService.initializeOwnerSession(ownerId);
    }

    @PostMapping("/owner/{ownerId}/login")
    @ResponseStatus(HttpStatus.OK)
    void logIn(@PathVariable OwnerId ownerId, @RequestBody CredentialsRequest request) {
        ownerSessionService.logIn(ownerId, request.credentials());
    }

    @PostMapping("/owner/{ownerId}/otp")
    @ResponseStatus(HttpStatus.OK)
    void inputOtp(@PathVariable OwnerId ownerId, @RequestBody OtpRequest request) {
        ownerSessionService.inputOtp(ownerId, request.otp());
    }

    //TODO asynchronise
    @GetMapping("/owner/{ownerId}/accounts")
    ResponseEntity<AccountsInfoResponse> fetchAccounts(@PathVariable OwnerId ownerId) {
        return ResponseEntity.ok(
            new AccountsInfoResponse(ownerAccountsService.fetchOwnerAccountsInfo(ownerId).accounts())
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

