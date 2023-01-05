package com.kontomatik.pko.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LibConfig {

    @Bean
    PkoScraperFacade pkoScraperFacade(PkoClient pkoClient) {
        return new PkoScraperFacade(
            new PkoLoginUseCase(pkoClient),
            new PkoAccountInfoUseCase(pkoClient)
        );
    }
}
