package com.kontomatik.pko.service.domain;

import java.util.UUID;

class SessionIdGenerator {
  static SessionId generate() {
    return new SessionId(UUID.randomUUID().toString());
  }
}
