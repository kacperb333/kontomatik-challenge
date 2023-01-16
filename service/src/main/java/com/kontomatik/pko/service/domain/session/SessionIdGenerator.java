package com.kontomatik.pko.service.domain.session;

import java.util.UUID;

class SessionIdGenerator {
  static OwnerSessionId generate() {
    return new OwnerSessionId(UUID.randomUUID().toString());
  }
}
