package com.kontomatik.pko.domain;

class PkoLoginUseCase {

    private final PkoClient pkoClient;

    public PkoLoginUseCase(PkoClient pkoClient) {
        this.pkoClient = pkoClient;
    }

    //TODO assert correct state after invoking client method
    LoginInProgressPkoSession logIn(Credentials credentials) {
        var inputUserNameResult = pkoClient.inputUserName(new PkoClient.PkoUserNameInput(credentials.login()));

        var inputPasswordResult = pkoClient.inputPassword(new PkoClient.PkoPasswordInput(
            inputUserNameResult.loginFlow(),
            credentials.password()
        ));

        return new LoginInProgressPkoSession(
            inputPasswordResult.loginFlow().internalSessionId(),
            inputPasswordResult.loginFlow().flowId(),
            inputPasswordResult.loginFlow().token()
        );
    }

    LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressLoginSession, Otp otp) {
        var inputOtpResult = pkoClient.inputOpt(new PkoClient.PkoOtpInput(
            new PkoClient.PkoInProgressLoginFlow(
                inProgressLoginSession.sessionId(),
                inProgressLoginSession.flowId(),
                inProgressLoginSession.token()
            ),
            otp.code()
        ));

        return new LoggedInPkoSession(inputOtpResult.internalSessionId());
    }
}
