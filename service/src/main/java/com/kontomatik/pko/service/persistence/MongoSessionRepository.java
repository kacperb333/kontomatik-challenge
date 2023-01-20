package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.service.common.DateTimeProvider;
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
    var persistentSession = PersistentSession.fromDomain(loginInProgressSession, dateTimeProvider.now());
    var saved = mongo.save(persistentSession);
    return new LoginInProgressSession(saved.sessionId, saved.pkoSession);
  }

  @Override
  public ImportFinishedSession save(ImportFinishedSession finishedSession) {
    var persistentSession = PersistentSession.fromDomain(finishedSession, dateTimeProvider.now());
    var saved = mongo.save(persistentSession);
    return new ImportFinishedSession(saved.sessionId, saved.accountsInfo);
  }

  @Override
  public ImportFailedSession save(ImportFailedSession importFailedSession) {
    var persistentSession = PersistentSession.fromDomain(importFailedSession, dateTimeProvider.now());
    var saved = mongo.save(persistentSession);
    return new ImportFailedSession(saved.sessionId);
  }

  @Override
  public LoginInProgressSession getLoginInProgressSession(SessionId sessionId) {
    var persistentSession = mongo.findById(sessionId, PersistentSession.class);
    if (persistentSession == null || persistentSession.pkoSession == null) {
      throw new SessionNotFound(sessionId);
    }
    return new LoginInProgressSession(persistentSession.sessionId, persistentSession.pkoSession);
  }

  @Override
  public AccountsInfo getSessionAccountsInfo(SessionId sessionId) {
    var persistentSession = mongo.findById(sessionId, PersistentSession.class);
    if (persistentSession == null) {
      throw new SessionNotFound(sessionId);
    }
    return persistentSession.accountsInfo;
  }
}
