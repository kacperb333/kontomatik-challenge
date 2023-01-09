package com.kontomatik.pko.domain;

class PkoAccountInfoUseCase {
    private final PkoClient pkoClient;

    PkoAccountInfoUseCase(PkoClient pkoClient) {
        this.pkoClient = pkoClient;
    }

    AccountsInfo fetchAccountInfo(LoggedInPkoSession session) {
        return pkoClient.fetchAccounts(new PkoFetchAccountsInput(session.sessionId()));
    }
}
