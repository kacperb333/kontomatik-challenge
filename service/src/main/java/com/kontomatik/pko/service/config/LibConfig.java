package com.kontomatik.pko.service.config;

import com.kontomatik.pko.lib.PkoScraperFacade;
import com.kontomatik.pko.lib.PkoScraperFacadeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LibConfig {
  @Bean
  PkoScraperFacade pkoScraperFacade() {
    return PkoScraperFacadeFactory.pkoScraperFacade();
  }
}
