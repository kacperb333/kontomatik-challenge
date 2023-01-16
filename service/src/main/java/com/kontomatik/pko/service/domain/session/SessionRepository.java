package com.kontomatik.pko.service.domain.session;

import java.util.Optional;

public interface SessionRepository {
  InitialSession store(InitialSession initialOwnerSession);

  LoginInProgressSession store(LoginInProgressSession loginInProgressOwnerSession);

  LoggedInSession store(LoggedInSession loggedInOwnerSession);

  FinishedSession store(FinishedSession finishedOwnerSession);

  Optional<InitialSession> fetchInitialOwnerSession(OwnerSessionId ownerId);

  Optional<LoginInProgressSession> fetchLoginInProgressOwnerSession(OwnerSessionId ownerId);

  Optional<LoggedInSession> fetchLoggedInOwnerSession(OwnerSessionId ownerId);
}
