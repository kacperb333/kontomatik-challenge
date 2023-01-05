package com.kontomatik.pko.domain;

public class PkoScraperFacade {

    private final PkoLoginUseCase pkoLoginUseCase;
    private final PkoAccountInfoUseCase pkoAccountInfoUseCase;

    public PkoScraperFacade(PkoLoginUseCase pkoLoginUseCase, PkoAccountInfoUseCase pkoAccountInfoUseCase) {
        this.pkoLoginUseCase = pkoLoginUseCase;
        this.pkoAccountInfoUseCase = pkoAccountInfoUseCase;
    }

    LoginInProgressPkoSession logIn(Credentials credentials) {
        return pkoLoginUseCase.logIn(credentials);
    }

    LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressSession, Otp otp) {
        return pkoLoginUseCase.inputOtp(inProgressSession, otp);
    }

    AccountsInfo fetchAccountsInfo(LoggedInPkoSession loggedInSession) {
        return pkoAccountInfoUseCase.fetchAccountInfo(loggedInSession);
    }
}

