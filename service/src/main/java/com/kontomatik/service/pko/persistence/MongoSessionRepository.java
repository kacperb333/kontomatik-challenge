package com.kontomatik.service.pko.persistence;

import com.kontomatik.service.common.DateTimeProvider;
import com.kontomatik.service.pko.domain.*;
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
  public OtpRequiredSession save(OtpRequiredSession otpRequiredSession) {
    var persistentSession = PersistentSession.fromDomain(otpRequiredSession, dateTimeProvider.now());
    var saved = mongo.save(persistentSession);
    return new OtpRequiredSession(saved.sessionId, saved.pkoSession);
  }

  @Override
  public ImportFinishedSession save(ImportFinishedSession finishedSession) {
    var persistentSession = PersistentSession.fromDomain(finishedSession, dateTimeProvider.now());
    var saved = mongo.save(persistentSession);
    return new ImportFinishedSession(saved.sessionId, saved.accounts);
  }

  @Override
  public ImportFailedSession save(ImportFailedSession importFailedSession) {
    var persistentSession = PersistentSession.fromDomain(importFailedSession, dateTimeProvider.now());
    var saved = mongo.save(persistentSession);
    return new ImportFailedSession(saved.sessionId);
  }

  @Override
  public OtpRequiredSession getOtpRequiredSession(SessionId sessionId) {
    var persistentSession = mongo.findById(sessionId, PersistentSession.class);
    if (persistentSession == null || persistentSession.pkoSession == null) {
      throw new SessionNotFound(sessionId);
    }
    return new OtpRequiredSession(persistentSession.sessionId, persistentSession.pkoSession);
  }

  @Override
  public AccountsImport getSessionAccountsImport(SessionId sessionId) {
    var persistentSession = mongo.findById(sessionId, PersistentSession.class);
    if (persistentSession == null) {
      throw new SessionNotFound(sessionId);
    }
    return new AccountsImport(persistentSession.isFailed, persistentSession.accounts);
  }
}
