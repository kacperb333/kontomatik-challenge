package com.kontomatik.service.pko.config;

import com.kontomatik.lib.pko.PkoScraperFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LibConfig {
  @Bean
  PkoScraperFacade pkoScraperFacade() {
    return PkoScraperFacade.pkoScraperFacade();
  }
}
