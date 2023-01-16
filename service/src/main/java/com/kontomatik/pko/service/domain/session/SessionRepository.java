package com.kontomatik.pko.service.domain.session;

import java.util.Optional;

public interface SessionRepository {
  InitialSession store(InitialSession initialSession);

  LoginInProgressSession store(LoginInProgressSession loginInProgressSession);

  LoggedInSession store(LoggedInSession loggedInSession);

  FinishedSession store(FinishedSession finishedSession);

  Optional<InitialSession> fetchInitialSession(SessionId sessionId);

  Optional<LoginInProgressSession> fetchLoginInProgressSession(SessionId sessionId);

  Optional<LoggedInSession> fetchLoggedInSession(SessionId sessionId);
}
