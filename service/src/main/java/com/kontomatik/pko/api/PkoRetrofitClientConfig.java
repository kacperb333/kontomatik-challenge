package com.kontomatik.pko.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class PkoRetrofitClientConfig {

    @Bean
    Retrofit retrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();

        return new Retrofit.Builder()
            .baseUrl("https://www.ipko.pl/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();
    }

    @Bean
    PkoRetrofitClient pkoRetrofitClient(Retrofit retrofit) {
        return retrofit.create(PkoRetrofitClient.class);
    }
}
