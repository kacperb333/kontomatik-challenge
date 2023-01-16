package com.kontomatik.pko.service.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

interface PersistentLoginInProgressSessionRepository
  extends MongoRepository<PersistentLoginInProgressSession, String> {
}
