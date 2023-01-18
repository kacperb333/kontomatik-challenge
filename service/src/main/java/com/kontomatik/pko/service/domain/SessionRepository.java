package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

public interface SessionRepository {

  LoginInProgressSession save(LoginInProgressSession loginInProgressSession);

  ImportFinishedSession save(ImportFinishedSession finishedSession);

  ImportFailedSession save(ImportFailedSession importFailedSession);

  LoginInProgressSession getLoginInProgressSession(SessionId sessionId);

  AccountsInfo getSessionAccountsInfo(SessionId sessionId);
}
