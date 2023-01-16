package com.kontomatik.pko.service.config;

import com.kontomatik.pko.lib.PkoScraperFacade;
import com.kontomatik.pko.lib.PkoScraperFacadeFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LibConfig {
  @Bean
  PkoScraperFacade pkoScraperFacade(@Value("${pko-client.base-url}") String baseUrl) {
    return PkoScraperFacadeFactory.pkoScraperFacade(baseUrl);
  }
}
