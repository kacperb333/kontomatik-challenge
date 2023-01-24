package com.kontomatik.service.pko

import com.kontomatik.lib.pko.domain.accounts.Account
import com.kontomatik.lib.pko.domain.accounts.Accounts
import com.kontomatik.lib.pko.domain.signin.*

class PkoLibFactories {
  static PkoSessionId testPkoSessionId() {
    return new PkoSessionId("test-pko-session-id")
  }

  static OtpRequiredPkoSession testOtpRequiredPkoSession() {
    return new OtpRequiredPkoSession(
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

  static Accounts testAccounts() {
    new Accounts([
      new Account(
        new Account.Name("account-1"),
        new Account.Balance(
          new Account.Balance.Amount("31.00"),
          new Account.Balance.Currency("PLN")
        )
      ),
      new Account(
        new Account.Name("account-2"),
        new Account.Balance(
          new Account.Balance.Amount("32.00"),
          new Account.Balance.Currency("USD")
        )
      )
    ])
  }
}
