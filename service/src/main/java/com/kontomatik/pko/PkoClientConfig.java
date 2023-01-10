package com.kontomatik.pko;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

@Configuration
public class PkoClientConfig {

    @Bean
    Retrofit retrofit() {
        return PkoRetrofitClientFactory.defaultRetrofit(
            PkoRetrofitClientFactory.defaultOkHttp()
        );
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
