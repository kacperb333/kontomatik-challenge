package com.kontomatik.pko.lib.usecase.login;

import com.kontomatik.pko.lib.client.PkoClient;

import java.util.List;
import java.util.function.Supplier;

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

  private void assertUserNameResult(PkoClient.PkoInProgressLoginResult result) {
    genericAssertForStep(
      "username",
      List.of(
        new AssertionPredicate("State ID", () -> "password".equals(result.assertionData().stateId())),
        new AssertionPredicate("Flow ID", () -> result.loginFlow().flowId() != null),
        new AssertionPredicate("Token", () -> result.loginFlow().token() != null),
        new AssertionPredicate("PKO Session ID", () -> result.loginFlow().pkoSessionId() != null)
      )
    );
  }

  private void assertPasswordResult(PkoClient.PkoInProgressLoginResult result) {
    genericAssertForStep(
      "password",
      List.of(
        new AssertionPredicate("State ID", () -> "one_time_password".equals(result.assertionData().stateId())),
        new AssertionPredicate("Flow ID", () -> result.loginFlow().flowId() != null),
        new AssertionPredicate("Token", () -> result.loginFlow().token() != null),
        new AssertionPredicate("PKO Session ID", () -> result.loginFlow().pkoSessionId() != null)
      )
    );
  }

  public LoggedInPkoSession inputOtp(LoginInProgressPkoSession inProgressLoginSession, Otp otp) {
    var inputOtpResult = pkoClient.inputOtp(new PkoClient.PkoOtpInput(
      new PkoClient.PkoLoginInProgressFlow(
        inProgressLoginSession.pkoSessionId(),
        inProgressLoginSession.flowId(),
        inProgressLoginSession.token()
      ),
      otp.code()
    ));
    assertOtpResult(inputOtpResult);

    return new LoggedInPkoSession(inputOtpResult.pkoSessionId());
  }

  private void assertOtpResult(PkoClient.PkoSuccessfulLoginResult result) {
    genericAssertForStep(
      "otp",
      List.of(
        new AssertionPredicate("State ID", () -> "END".equals(result.assertionData().stateId())),
        new AssertionPredicate("Finished", () -> result.assertionData().finished()),
        new AssertionPredicate("PKO Session ID", () -> result.pkoSessionId() != null)
      )
    );
  }

  private void genericAssertForStep(String lastStep, List<AssertionPredicate> predicates) {
    var assertionResults = predicates.stream()
      .map(AssertionPredicate::doAssert)
      .toList();

    if (assertionResults.stream().anyMatch(it -> it instanceof Fail)) {
      throw UnexpectedAction.fromAssertionResults(lastStep, assertionResults);
    }
  }

  private record AssertionPredicate(String name, Supplier<Boolean> predicate) {
    AssertionResult doAssert() {
      if (predicate.get()) {
        return new Success(name);
      } else {
        return new Fail(name);
      }
    }
  }

  record Success(String name) implements AssertionResult {
    @Override
    public String message() {
      return String.format("Successful step [%s], value as expected.", name);
    }
  }

  record Fail(String name) implements AssertionResult {
    @Override
    public String message() {
      return String.format("Failed step [%s], value does not hold required predicate", name);
    }
  }

  interface AssertionResult {
    String message();
  }
}
