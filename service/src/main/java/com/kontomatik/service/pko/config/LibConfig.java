package com.kontomatik.service.pko.config;

import com.kontomatik.lib.pko.PkoScraperFacade;
import com.kontomatik.lib.pko.PkoScraperFacadeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LibConfig {
  @Bean
  PkoScraperFacade pkoScraperFacade() {
    return PkoScraperFacadeFactory.pkoScraperFacade();
  }
}
