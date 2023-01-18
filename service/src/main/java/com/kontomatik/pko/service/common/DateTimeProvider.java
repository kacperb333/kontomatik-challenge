package com.kontomatik.pko.service.common;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DateTimeProvider {
  public Instant now() {
    return Instant.now();
  }
}
