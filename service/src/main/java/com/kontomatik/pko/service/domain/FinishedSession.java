package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

public interface FinishedSession {
  SessionId sessionId();

  AccountsInfo accountsInfo();
}
