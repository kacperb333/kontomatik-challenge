package com.kontomatik.pko;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

@Configuration
public class PkoClientConfig {

    @Bean
    Retrofit retrofit(@Value("${pko-client.base-url}") String baseUrl) {
        return PkoRetrofitClientFactory.defaultRetrofit(
            PkoRetrofitClientFactory.defaultOkHttp(),
            baseUrl
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
