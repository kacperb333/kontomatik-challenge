package com.kontomatik.pko.lib.client;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.lib.usecase.login.FlowId;
import com.kontomatik.pko.lib.usecase.login.PkoSessionId;
import com.kontomatik.pko.lib.usecase.login.Token;

public interface PkoClient {
  PkoInProgressLoginResult inputUserName(PkoUserNameInput userNameInput);

  PkoInProgressLoginResult inputPassword(PkoPasswordInput passwordInput);

  PkoSuccessfulLoginResult inputOtp(PkoOtpInput otpInput);

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
    PkoSessionId pkoSessionId,
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
    boolean finished
  ) {
  }

  record PkoFetchAccountsInput(
    PkoSessionId pkoSessionId
  ) {
  }

  class PkoClientIOException extends RuntimeException {
    public PkoClientIOException(Throwable cause) {
      super(cause);
    }
  }
}
