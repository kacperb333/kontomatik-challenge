package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.PkoScraperFacade;
import com.kontomatik.pko.lib.usecase.login.Credentials;
import com.kontomatik.pko.lib.usecase.login.Otp;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class SessionService {

  private final PkoScraperFacade pkoScraperFacade;
  private final SessionRepository ownerSessionRepository;

  SessionService(
    PkoScraperFacade pkoScraperFacade,
    SessionRepository ownerSessionRepository
  ) {
    this.pkoScraperFacade = pkoScraperFacade;
    this.ownerSessionRepository = ownerSessionRepository;
  }

  public InitialSession initializeOwnerSession(OwnerId ownerId) {
    var generatedSessionId = SessionIdGenerator.generate();
    return ownerSessionRepository.store(new InitialSession(generatedSessionId, ownerId));
  }

  public LoginInProgressSession logIn(OwnerSessionId ownerSessionId, Credentials credentials) {
    ownerSessionRepository.fetchInitialOwnerSession(ownerSessionId);
    var loginInProgressSession = ownerSessionRepository.fetchInitialOwnerSession(ownerSessionId)
      .map(initialOwnerSession -> initialOwnerSession.initializeLogIn(pkoScraperFacade.logIn(credentials)))
      .orElseThrow(() -> new SessionNotInitialized(ownerSessionId));

    return ownerSessionRepository.store(loginInProgressSession);
  }

  public LoggedInSession inputOtp(OwnerSessionId ownerSessionId, Otp otp) {
    var loggedInSession = ownerSessionRepository.fetchLoginInProgressOwnerSession(ownerSessionId)
      .map(inProgressOwnerSession ->
        inProgressOwnerSession.finishLogin(pkoScraperFacade.inputOtp(
          inProgressOwnerSession.asLoginInProgressPkoSession(),
          otp
        ))
      )
      .orElseThrow(() -> new FinishedSession.SessionLoginNotInProgress(ownerSessionId));

    return ownerSessionRepository.store(loggedInSession);
  }

  public void doWithinOwnerSession(OwnerSessionId ownerSessionId, Consumer<LoggedInSession> sessionConsumer) {
    var loggedInOwnerSession = ownerSessionRepository.fetchLoggedInOwnerSession(ownerSessionId)
      .orElseThrow(() -> new SessionNotLoggedIn(ownerSessionId));

    var finishedSession = loggedInOwnerSession.doWithinSession(sessionConsumer);

    ownerSessionRepository.store(finishedSession);
  }
}
