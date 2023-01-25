package com.kontomatik.service.pko.persistence.session;

import com.kontomatik.service.common.DateTimeProvider;
import com.kontomatik.service.pko.domain.OtpRequiredSession;
import com.kontomatik.service.pko.domain.SessionId;
import com.kontomatik.service.pko.domain.SessionNotFound;
import com.kontomatik.service.pko.domain.SessionRepository;
import org.springframework.stereotype.Repository;

@Repository
class MongoSessionRepository implements SessionRepository {
  private final SpringPersistentSessionRepository sessionRepository;
  private final DateTimeProvider dateTimeProvider;

  MongoSessionRepository(
    SpringPersistentSessionRepository sessionRepository,
    DateTimeProvider dateTimeProvider
  ) {
    this.sessionRepository = sessionRepository;
    this.dateTimeProvider = dateTimeProvider;
  }

  @Override
  public OtpRequiredSession save(OtpRequiredSession otpRequiredSession) {
    var persistentSession = PersistentSession.fromDomain(otpRequiredSession, dateTimeProvider.now());
    var saved = sessionRepository.save(persistentSession);
    return new OtpRequiredSession(saved.sessionId, saved.pkoSession);
  }

  @Override
  public OtpRequiredSession getOtpRequiredSession(SessionId sessionId) {
    return sessionRepository.findById(sessionId)
      .map(it -> new OtpRequiredSession(it.sessionId, it.pkoSession))
      .orElseThrow(() -> new SessionNotFound(sessionId));
  }
}
