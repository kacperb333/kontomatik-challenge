package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

sealed public interface FinishedSession permits ImportFailedSession, ImportFinishedSession {
  SessionId sessionId();

  AccountsInfo accountsInfo();
}
