package com.kontomatik.pko;

public class PkoAccountInfoUseCase {
    private final PkoClient pkoClient;

    public PkoAccountInfoUseCase(PkoClient pkoClient) {
        this.pkoClient = pkoClient;
    }

    public AccountsInfo fetchAccountInfo(LoggedInPkoSession session) {
        return pkoClient.fetchAccounts(new PkoClient.PkoFetchAccountsInput(session.pkoSessionId()));
    }
}
