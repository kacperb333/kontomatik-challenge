package com.kontomatik.lib.pko.domain.accounts;

import java.math.BigDecimal;

public record Account(
  Name name,
  Balance balance
) {

  public static Account from(String name, String amount, String currency) {
    return new Account(
      new Name(name),
      new Balance(
        new Balance.Amount(new BigDecimal(amount)),
        new Balance.Currency(currency)
      )
    );
  }

  public record Name(
    String value
  ) {
  }

  public record Balance(
    Amount amount,
    Currency currency
  ) {
    public record Amount(
      BigDecimal value
    ) {
    }

    public record Currency(
      String value
    ) {
    }
  }
}
