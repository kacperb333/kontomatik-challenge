package com.kontomatik.pko.service

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo
import com.kontomatik.pko.lib.usecase.login.*

class LibFactories {
  static PkoSessionId testPkoSessionId() {
    return new PkoSessionId("test-pko-session-id")
  }

  static LoginInProgressPkoSession testLoginInProgressPkoSession() {
    return new LoginInProgressPkoSession(
      testPkoSessionId(),
      new FlowId("test-flow-id"),
      new Token("test-token")
    )
  }

  static LoggedInPkoSession testLoggedInPkoSession() {
    return new LoggedInPkoSession(
      testPkoSessionId()
    )
  }

  static AccountsInfo testAccountsInfo() {
    new AccountsInfo([
      new AccountInfo("account-1", "31.00", "PLN"),
      new AccountInfo("account-2", "32.00", "USD")
    ])
  }
}
