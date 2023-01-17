package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.DateTimeProvider;
import com.kontomatik.pko.service.domain.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

@Repository
class MongoSessionRepository implements SessionRepository {
  private final MongoOperations mongo;
  private final DateTimeProvider dateTimeProvider;

  MongoSessionRepository(
    MongoOperations mongo,
    DateTimeProvider dateTimeProvider
  ) {
    this.mongo = mongo;
    this.dateTimeProvider = dateTimeProvider;
  }

  @Override
  public LoginInProgressSession save(LoginInProgressSession loginInProgressSession) {
    var persistentSession = PersistentLoginInProgressSession.fromDomain(loginInProgressSession, dateTimeProvider.now());
    return mongo.save(persistentSession).toDomain();
  }

  @Override
  public FinishedSession save(FinishedSession finishedSession) {
    var persistentSession = switch (finishedSession) {
      case ImportFinishedSession s -> PersistentImportFinishedSession.fromDomain(s, dateTimeProvider.now());
      case ImportFailedSession s -> PersistentImportFailedSession.fromDomain(s, dateTimeProvider.now());
    };
    return mongo.save(persistentSession).toDomain();
  }

  @Override
  public LoginInProgressSession getLoginInProgressSession(SessionId sessionId) {
    var persistentSession = mongo.findById(sessionId.value(), PersistentLoginInProgressSession.class);
    if (persistentSession == null) {
      throw new SessionNotFound(sessionId);
    }
    return persistentSession.toDomain();
  }

  @Override
  public FinishedSession getFinishedSession(SessionId sessionId) {
    var persistentSession = mongo.findById(sessionId.value(), PersistentFinishedSession.class);
    if (persistentSession == null) {
      throw new SessionNotFound(sessionId);
    }
    return persistentSession.toDomain();
  }
}
