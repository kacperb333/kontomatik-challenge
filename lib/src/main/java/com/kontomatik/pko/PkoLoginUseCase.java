package com.kontomatik.pko;

import java.util.List;

public class PkoLoginUseCase {

    private final PkoClient pkoClient;

    public PkoLoginUseCase(PkoClient pkoClient) {
        this.pkoClient = pkoClient;
    }

    public LoginInProgressPkoSession logIn(Credentials credentials) {
        var inputUserNameResult = pkoClient.inputUserName(new PkoClient.PkoUserNameInput(credentials.login()));
        assertUserNameResult(inputUserNameResult);

        var inputPasswordResult = pkoClient.inputPassword(new PkoClient.PkoPasswordInput(
            inputUserNameResult.loginFlow(),
            credentials.password()
        ));
        assertPasswordResult(inputPasswordResult);

        return new LoginInProgressPkoSession(
            inputPasswordResult.loginFlow().pkoSessionId(),
            inputPasswordResult.loginFlow().flowId(),
            inputPasswordResult.loginFlow().token()
        );
    }

    public LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressLoginSession, Otp otp) {
        var inputOtpResult = pkoClient.inputOpt(new PkoClient.PkoOtpInput(
            new PkoClient.PkoLoginInProgressFlow(
                inProgressLoginSession.pkoSessionId(),
                inProgressLoginSession.flowId(),
                inProgressLoginSession.token()
            ),
            otp.code()
        ));
        assertOtpResult(inputOtpResult);

        return new LoggedInPkoSession(inputOtpResult.internalSessionId());
    }

    private void assertUserNameResult(PkoClient.PkoInProgressLoginResult result) {
        genericAssertForStep(
            "username",
            List.of(
                new AssertionPredicate<>("State ID", "password", result.assertionData().stateId())
            )
        );
    }

    private void assertPasswordResult(PkoClient.PkoInProgressLoginResult result) {
        genericAssertForStep(
            "password",
            List.of(
                new AssertionPredicate<>("State ID", "one_time_password", result.assertionData().stateId())
            )
        );
    }

    private void assertOtpResult(PkoClient.PkoSuccessfulLoginResult result) {
        genericAssertForStep(
            "otp",
            List.of(
                new AssertionPredicate<>("State ID", "END", result.assertionData().stateId()),
                new AssertionPredicate<>("Finished", true, result.assertionData().finished())
            )
        );
    }

    private <T> void genericAssertForStep(String lastStep, List<AssertionPredicate<T>> predicates) {
        var assertionResults = predicates.stream()
            .map(AssertionPredicate::doAssert)
            .toList();

        if (assertionResults.stream().anyMatch(it -> it instanceof Fail)) {
            throw UnexpectedAction.fromAssertionResults(lastStep, assertionResults);
        }
    }

    private record AssertionPredicate<T>(String name, T expected, T toCheck) {
        AssertionResult doAssert() {
            if (expected.equals(toCheck)) {
                return new Success<>(name, expected);
            } else {
                return new Fail<>(name, expected, toCheck);
            }
        }
    }

    record Success<T>(String name, T expected) implements AssertionResult {
        @Override
        public String message() {
            return String.format("Successful step [%s], value as expected [%s]", name, expected);
        }
    }

    record Fail<T>(String name, T expected, T actual) implements AssertionResult {
        @Override
        public String message() {
            return String.format("Failed step [%s], expected value [%s], but got [%s]", name, expected, actual);
        }
    }

    interface AssertionResult {
        String message();
    }
}
