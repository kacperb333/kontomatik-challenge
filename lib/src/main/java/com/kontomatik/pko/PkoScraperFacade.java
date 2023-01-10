package com.kontomatik.pko;

public class PkoScraperFacade {

    private final PkoLoginUseCase pkoLoginUseCase;
    private final PkoAccountInfoUseCase pkoAccountInfoUseCase;

    public PkoScraperFacade(PkoLoginUseCase pkoLoginUseCase, PkoAccountInfoUseCase pkoAccountInfoUseCase) {
        this.pkoLoginUseCase = pkoLoginUseCase;
        this.pkoAccountInfoUseCase = pkoAccountInfoUseCase;
    }

    public LoginInProgressPkoSession logIn(Credentials credentials) {
        return pkoLoginUseCase.logIn(credentials);
    }

    public LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressSession, Otp otp) {
        return pkoLoginUseCase.inputOtp(inProgressSession, otp);
    }

    public AccountsInfo fetchAccountsInfo(LoggedInPkoSession loggedInSession) {
        return pkoAccountInfoUseCase.fetchAccountInfo(loggedInSession);
    }
}

