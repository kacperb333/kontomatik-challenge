package com.kontomatik.pko;

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
        PkoLoginInProgressFlow loginFlow,
        String password
    ) {
    }

    record PkoOtpInput(
        PkoLoginInProgressFlow loginFlow,
        String code
    ) {
    }

    record PkoInProgressLoginResult(
        PkoLoginInProgressFlow loginFlow,
        PkoInProgressLoginAssertionData assertionData
    ) {
    }

    record PkoSuccessfulLoginResult(
        PkoSessionId internalSessionId,
        PkoSuccessfulLoginAssertionData assertionData
    ) {
    }

    record PkoLoginInProgressFlow(
        PkoSessionId pkoSessionId,
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
        PkoSessionId internalSessionId
    ) {
    }

    class PkoClientIOException extends RuntimeException {
        public PkoClientIOException(Throwable cause) {
            super(cause);
        }
    }
}
