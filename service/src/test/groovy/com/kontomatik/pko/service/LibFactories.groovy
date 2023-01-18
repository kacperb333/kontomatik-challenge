package com.kontomatik.pko.service

import com.kontomatik.pko.lib.usecase.accounts.AccountInfo
import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo
import com.kontomatik.pko.lib.usecase.login.FlowId
import com.kontomatik.pko.lib.usecase.login.LoginInProgressPkoSession
import com.kontomatik.pko.lib.usecase.login.PkoSessionId
import com.kontomatik.pko.lib.usecase.login.Token

class LibFactories {
  static LoginInProgressPkoSession testLoginInProgressPkoSession() {
    return new LoginInProgressPkoSession(
      new PkoSessionId("test-pko-session-id"),
      new FlowId("test-flow-id"),
      new Token("test-token")
    )
  }

  static AccountsInfo testAccountsInfo() {
    new AccountsInfo([
      new AccountInfo("account-1", "31.00", "PLN"),
      new AccountInfo("account-2", "32.00", "USD")
    ])
  }
}
