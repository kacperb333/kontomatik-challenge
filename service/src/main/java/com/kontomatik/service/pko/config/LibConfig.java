package com.kontomatik.service.pko.config;

import com.kontomatik.lib.pko.ScraperFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LibConfig {
  @Bean
  ScraperFacade scraperFacade() {
    return ScraperFacade.scraperFacade();
  }
}
