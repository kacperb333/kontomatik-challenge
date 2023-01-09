package com.kontomatik.pko.adapters;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.util.Map;

interface PkoRetrofitClient {

    @Headers({
        "accept: application/json",
        "content-type: application/json"
    })
    @POST("/ipko3/login")
    Call<PkoLoginResponse> inputLogin(@Body PkoRestClient.PkoRequest<PkoRestClient.PkoLoginData> request);

    @Headers({
        "accept: application/json",
        "content-type: application/json"
    })
    @POST("/ipko3/login")
    Call<PkoLoginResponse> inputPassword(
        @Header("x-session-id") String pkoSessionId,
        @Body PkoRestClient.PkoRequest<PkoRestClient.PkoPasswordData> request
    );

    @Headers({
        "accept: application/json",
        "content-type: application/json"
    })
    @POST("/ipko3/login")
    Call<PkoLoginResponse> inputOtp(
        @Header("x-session-id") String pkoSessionId,
        @Body PkoRestClient.PkoRequest<PkoRestClient.PkoOtpData> request
    );

    @Headers({
        "accept: application/json",
        "content-type: application/json"
    })
    @POST("/ipko3/init")
    Call<PkoAccountsResponse> fetchAccounts(
        @Header("x-session-id") String pkoSessionId,
        @Body PkoRestClient.PkoRequest<PkoRestClient.PkoFetchAccountsData> request
    );

    class PkoLoginResponse {
        public final String flow_id;
        public final String token;
        public final String state_id;
        public final Boolean finished;

        public PkoLoginResponse(String flow_id, String token, String state_id, Boolean finished) {
            this.flow_id = flow_id;
            this.token = token;
            this.state_id = state_id;
            this.finished = finished;
        }
    }

    class PkoAccountsResponse {
        public final AccountsDataResponse response;

        public PkoAccountsResponse(AccountsDataResponse response) {
            this.response = response;
        }
    }

    class AccountsDataResponse {
        public final AccountsResponse data;

        public AccountsDataResponse(AccountsResponse data) {
            this.data = data;
        }
    }

    class AccountsResponse {
        public final Map<String, AccountResponse> accounts;

        public AccountsResponse(Map<String, AccountResponse> accounts) {
            this.accounts = accounts;
        }
    }

    class AccountResponse {
        public final String bank_name;
        public final String name;
        public final String balance;
        public final String currency;

        public AccountResponse(String bank_name, String name, String balance, String currency) {
            this.bank_name = bank_name;
            this.name = name;
            this.balance = balance;
            this.currency = currency;
        }
    }
}
