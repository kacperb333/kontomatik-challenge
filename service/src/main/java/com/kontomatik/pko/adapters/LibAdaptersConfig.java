package com.kontomatik.pko.adapters;

import com.kontomatik.pko.domain.PkoClient;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

@Configuration
class LibAdaptersConfig {

    @Bean
    OkHttpClient okHttpClient() {
        return PkoRetrofitClientFactory.defaultOkHttp();
    }

    @Bean
    Retrofit retrofit(OkHttpClient okHttpClient) {
        return PkoRetrofitClientFactory.defaultRetrofit(okHttpClient);
    }

    @Bean
    PkoRetrofitClient pkoRetrofitClient(Retrofit retrofit) {
        return PkoRetrofitClientFactory.pkoRetrofitClient(retrofit);
    }

    @Bean
    PkoClient pkoClient(PkoRetrofitClient pkoRetrofitClient) {
        return PkoRetrofitClientFactory.defaultPkoClient(pkoRetrofitClient);
    }
}
