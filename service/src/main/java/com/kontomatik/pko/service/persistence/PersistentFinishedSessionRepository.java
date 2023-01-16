package com.kontomatik.pko.service.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

interface PersistentFinishedSessionRepository
  extends MongoRepository<PersistentFinishedSession, String> {
}
