package com.kontomatik.pko.lib.usecase.accounts;

public record AccountInfo(
  String name,
  String balance,
  String currency
) {
}
