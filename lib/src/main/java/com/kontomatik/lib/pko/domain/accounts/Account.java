package com.kontomatik.lib.pko.domain.accounts;

public record Account(
  Name name,
  Balance balance
) {

  public record Name(
    String value
  ) {
  }

  public record Balance(
    Amount amount,
    Currency currency
  ) {
    public record Amount(
      String value
    ) {
    }

    public record Currency(
      String value
    ) {
    }
  }
}
