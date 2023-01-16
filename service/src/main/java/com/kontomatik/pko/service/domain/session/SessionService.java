package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.PkoScraperFacade;
import com.kontomatik.pko.lib.usecase.login.Credentials;
import com.kontomatik.pko.lib.usecase.login.Otp;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class SessionService {

  private final PkoScraperFacade pkoScraperFacade;
  private final SessionRepository sessionRepository;

  SessionService(
    PkoScraperFacade pkoScraperFacade,
    SessionRepository sessionRepository
  ) {
    this.pkoScraperFacade = pkoScraperFacade;
    this.sessionRepository = sessionRepository;
  }

  public InitialSession initializeSession() {
    var generatedSessionId = SessionIdGenerator.generate();
    return sessionRepository.store(new InitialSession(generatedSessionId));
  }

  public LoginInProgressSession logIn(SessionId sessionId, Credentials credentials) {
    sessionRepository.fetchInitialSession(sessionId);
    var loginInProgressSession = sessionRepository.fetchInitialSession(sessionId)
      .map(initialSession -> initialSession.initializeLogIn(pkoScraperFacade.logIn(credentials)))
      .orElseThrow(() -> new SessionNotInitialized(sessionId));

    return sessionRepository.store(loginInProgressSession);
  }

  public LoggedInSession inputOtp(SessionId sessionId, Otp otp) {
    var loggedInSession = sessionRepository.fetchLoginInProgressSession(sessionId)
      .map(inProgressSession ->
        inProgressSession.finishLogin(pkoScraperFacade.inputOtp(
          inProgressSession.asLoginInProgressPkoSession(),
          otp
        ))
      )
      .orElseThrow(() -> new FinishedSession.SessionLoginNotInProgress(sessionId));

    return sessionRepository.store(loggedInSession);
  }

  public void doWithinSession(SessionId sessionId, Consumer<LoggedInSession> sessionConsumer) {
    var loggedInSession = sessionRepository.fetchLoggedInSession(sessionId)
      .orElseThrow(() -> new SessionNotLoggedIn(sessionId));

    var finishedSession = loggedInSession.doWithinSession(sessionConsumer);

    sessionRepository.store(finishedSession);
  }
}
