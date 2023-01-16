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

  public LoginInProgressSession logIn(Credentials credentials) {
    InitialSession initialSession = createInitialSession();
    LoginInProgressSession loginInProgressSession = initialSession.initializeLogIn(pkoScraperFacade.logIn(credentials));
    return sessionRepository.save(loginInProgressSession);
  }

  private static InitialSession createInitialSession() {
    return new InitialSession(SessionIdGenerator.generate());
  }

  public LoggedInSession inputOtp(SessionId sessionId, Otp otp) {
    LoginInProgressSession loginInProgressSession = sessionRepository.getLoginInProgressSession(sessionId);
    LoggedInSession loggedInSession = loginInProgressSession.finishLogin(
      pkoScraperFacade.inputOtp(
        loginInProgressSession.asLoginInProgressPkoSession(),
        otp
      )
    );
    return sessionRepository.save(loggedInSession);
  }

  public void doWithinSession(SessionId sessionId, Consumer<LoggedInSession> sessionConsumer) {
    LoggedInSession loggedInSession = sessionRepository.getLoggedInSession(sessionId);
    FinishedSession finishedSession = loggedInSession.doWithinSession(sessionConsumer);
    sessionRepository.save(finishedSession);
  }
}
