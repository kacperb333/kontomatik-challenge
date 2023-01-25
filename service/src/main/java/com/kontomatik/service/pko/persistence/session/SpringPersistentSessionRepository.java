package com.kontomatik.service.pko.persistence.session;

import com.kontomatik.service.pko.domain.SessionId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringPersistentSessionRepository extends MongoRepository<PersistentSession, SessionId> {
}
