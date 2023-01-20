package com.kontomatik.service.pko

import com.kontomatik.lib.pko.domain.accounts.AccountInfo
import com.kontomatik.lib.pko.domain.accounts.AccountsInfo
import com.kontomatik.lib.pko.domain.login.FlowId
import com.kontomatik.lib.pko.domain.login.LoggedInPkoSession
import com.kontomatik.lib.pko.domain.login.LoginInProgressPkoSession
import com.kontomatik.lib.pko.domain.login.PkoSessionId
import com.kontomatik.lib.pko.domain.login.Token

class PkoLibFactories {
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
