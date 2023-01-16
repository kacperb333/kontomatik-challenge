package com.kontomatik.pko.lib.client;

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo;
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.lib.usecase.login.FlowId;
import com.kontomatik.pko.lib.usecase.login.PkoSessionId;
import com.kontomatik.pko.lib.usecase.login.Token;

import java.io.IOException;

class PkoRestClient implements PkoClient {

  private final PkoRetrofitClient retrofitClient;
  private static final int REQUEST_VERSION = 3;
  private static final String PKO_SESSION_HEADER = "x-session-id";

  public PkoRestClient(PkoRetrofitClient retrofitClient) {
    this.retrofitClient = retrofitClient;
  }

  @Override
  public PkoInProgressLoginResult inputUserName(PkoUserNameInput userNameInput) {
    try {
      var call = retrofitClient.inputLogin(
        PkoRequest.pkoLoginRequest(new PkoLoginData(userNameInput.userName()))
      );
      var response = call.execute();
      return new PkoInProgressLoginResult(
        new PkoLoginInProgressFlow(
          new PkoSessionId(response.headers().get(PKO_SESSION_HEADER)),
          new FlowId(response.body().flow_id),
          new Token(response.body().token)
        ),
        new PkoInProgressLoginAssertionData(
          response.body().state_id
        )
      );
    } catch (IOException e) {
      throw new PkoClientIOException(e);
    }
  }

  @Override
  public PkoInProgressLoginResult inputPassword(PkoPasswordInput passwordInput) {
    try {
      var call = retrofitClient.inputPassword(
        passwordInput.loginFlow().pkoSessionId().value(),
        PkoRequest.pkoPasswordRequest(
          passwordInput.loginFlow().flowId().value(),
          passwordInput.loginFlow().token().value(),
          new PkoPasswordData(passwordInput.password())
        )
      );
      var response = call.execute();
      return new PkoInProgressLoginResult(
        new PkoLoginInProgressFlow(
          new PkoSessionId(response.headers().get(PKO_SESSION_HEADER)),
          new FlowId(response.body().flow_id),
          new Token(response.body().token)
        ),
        new PkoInProgressLoginAssertionData(
          response.body().state_id
        )
      );
    } catch (IOException e) {
      throw new PkoClientIOException(e);
    }
  }

  @Override
  public PkoSuccessfulLoginResult inputOtp(PkoOtpInput otpInput) {
    try {
      var call = retrofitClient.inputOtp(
        otpInput.loginFlow().pkoSessionId().value(),
        PkoRequest.pkoOtpRequest(
          otpInput.loginFlow().flowId().value(),
          otpInput.loginFlow().token().value(),
          new PkoOtpData(otpInput.code())
        )
      );
      var response = call.execute();
      return new PkoSuccessfulLoginResult(
        new PkoSessionId(response.headers().get(PKO_SESSION_HEADER)),
        new PkoSuccessfulLoginAssertionData(
          response.body().state_id,
          response.body().finished
        )
      );
    } catch (IOException e) {
      throw new PkoClientIOException(e);
    }
  }

  @Override
  public AccountsInfo fetchAccounts(PkoFetchAccountsInput fetchAccountsInput) {
    try {
      var call = retrofitClient.fetchAccounts(
        fetchAccountsInput.pkoSessionId().value(),
        PkoRequest.pkoFetchAccountsRequest()
      );
      var response = call.execute();

      var accounts = response.body().response.data.accounts.values().stream()
        .map(accountResponse -> new AccountInfo(
          accountResponse.name,
          accountResponse.balance,
          accountResponse.currency
        ));

      return new AccountsInfo(accounts.toList());
    } catch (IOException e) {
      throw new PkoClientIOException(e);
    }
  }

  public record PkoRequest<T>(
    Integer version,
    String state_id,
    String action,
    String flow_id,
    String token,
    T data
  ) {
    static PkoRequest<PkoLoginData> pkoLoginRequest(PkoLoginData data) {
      return new PkoRequest<>(
        PkoRestClient.REQUEST_VERSION,
        "login",
        "submit",
        null,
        null,
        data
      );
    }

    static PkoRequest<PkoPasswordData> pkoPasswordRequest(String flow_id, String token, PkoPasswordData data) {
      return new PkoRequest<>(
        REQUEST_VERSION,
        "password",
        "submit",
        flow_id,
        token,
        data
      );
    }

    static PkoRequest<PkoOtpData> pkoOtpRequest(String flow_id, String token, PkoOtpData data) {
      return new PkoRequest<>(
        REQUEST_VERSION,
        "one_time_password",
        "submit",
        flow_id,
        token,
        data
      );
    }

    static PkoRequest<PkoFetchAccountsData> pkoFetchAccountsRequest() {
      return new PkoRequest<>(
        REQUEST_VERSION,
        null,
        null,
        null,
        null,
        PkoFetchAccountsData.defaultOptions()
      );
    }
  }

  public record PkoLoginData(
    String login
  ) {
  }

  public record PkoPasswordData(
    String password
  ) {
  }

  public record PkoOtpData(
    String otp
  ) {
  }

  public record PkoFetchAccountsData(
    PkoFetchAccountsOptions accounts
  ) {
    static PkoFetchAccountsData defaultOptions() {
      return new PkoFetchAccountsData(PkoFetchAccountsOptions.forceSynchronization());
    }
  }

  public record PkoFetchAccountsOptions(
    boolean force_synchronization
  ) {
    static PkoFetchAccountsOptions forceSynchronization() {
      return new PkoFetchAccountsOptions(true);
    }
  }
}
