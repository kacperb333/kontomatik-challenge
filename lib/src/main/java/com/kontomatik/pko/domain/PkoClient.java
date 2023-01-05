package com.kontomatik.pko.domain;

interface PkoClient {
    PkoInProgressLoginResult inputUserName(PkoUserNameInput userNameInput);

    PkoInProgressLoginResult inputPassword(PkoPasswordInput passwordInput);

    PkoSuccessfulLoginResult inputOpt(PkoOtpInput otpInput);

    AccountsInfo fetchAccounts(PkoFetchAccountsInput fetchAccountsInput);
}

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
    PkoInProgressLoginFlow loginFlow
) {
}

record PkoSuccessfulLoginResult(
    SessionId internalSessionId
) {
}

record PkoInProgressLoginFlow(
    SessionId internalSessionId,
    FlowId flowId,
    Token token
) {
}

record PkoFetchAccountsInput(
    SessionId internalSessionId
) {
}
