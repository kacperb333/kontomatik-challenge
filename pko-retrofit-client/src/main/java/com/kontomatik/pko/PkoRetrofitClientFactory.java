package com.kontomatik.pko;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PkoRetrofitClientFactory {

    public static OkHttpClient defaultOkHttp() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();
    }

    public static Retrofit defaultRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl("https://www.ipko.pl/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();
    }

    public static PkoRetrofitClient pkoRetrofitClient(Retrofit retrofit) {
        return retrofit.create(PkoRetrofitClient.class);
    }

    public static PkoClient defaultPkoClient(PkoRetrofitClient pkoRetrofitClient) {
        return new PkoRestClient(pkoRetrofitClient);
    }
}
