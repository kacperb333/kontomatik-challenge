package com.kontomatik.pko.domain;

public interface PkoClient {
    PkoInProgressLoginResult inputUserName(PkoUserNameInput userNameInput);

    PkoInProgressLoginResult inputPassword(PkoPasswordInput passwordInput);

    PkoSuccessfulLoginResult inputOpt(PkoOtpInput otpInput);

    AccountsInfo fetchAccounts(PkoFetchAccountsInput fetchAccountsInput);

    record PkoUserNameInput(
        String userName
    ) {
    }

    record PkoPasswordInput(
        PkoInProgressLoginFlow loginFlow,
        String password
    ) {
    }

    record PkoOtpInput(
        PkoInProgressLoginFlow loginFlow,
        String code
    ) {
    }

    record PkoInProgressLoginResult(
        PkoInProgressLoginFlow loginFlow,
        PkoInProgressLoginAssertionData assertionData
    ) {
    }

    record PkoSuccessfulLoginResult(
        SessionId internalSessionId,
        PkoSuccessfulLoginAssertionData assertionData
    ) {
    }

    record PkoInProgressLoginFlow(
        SessionId internalSessionId,
        FlowId flowId,
        Token token
    ) {
    }

    record PkoInProgressLoginAssertionData(
        String stateId
    ) {
    }

    record PkoSuccessfulLoginAssertionData(
        String stateId,
        Boolean finished
    ) {
    }

    record PkoFetchAccountsInput(
        SessionId internalSessionId
    ) {
    }

    class PkoClientIOException extends RuntimeException {
        public PkoClientIOException(Throwable cause) {
            super(cause);
        }
    }
}
