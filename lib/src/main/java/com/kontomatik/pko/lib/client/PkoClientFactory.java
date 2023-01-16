package com.kontomatik.pko.lib.client;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PkoClientFactory {

  public static PkoClient pkoClient(String baseUrl) {
    OkHttpClient okHttpClient = defaultOkHttp();
    Retrofit retrofit = defaultRetrofit(okHttpClient, baseUrl);
    return new PkoRestClient(retrofit.create(PkoRetrofitClient.class));
  }

  private static Retrofit defaultRetrofit(OkHttpClient okHttpClient, String baseUrl) {
    return new Retrofit.Builder()
      .baseUrl(baseUrl)
      .addConverterFactory(GsonConverterFactory.create())
      .client(okHttpClient)
      .build();
  }


  private static OkHttpClient defaultOkHttp() {
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    return new OkHttpClient.Builder()
      .addInterceptor(loggingInterceptor)
      .build();
  }
}
