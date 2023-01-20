package com.kontomatik.lib.pko.domain.accounts;

public record AccountInfo(
  String name,
  String balance,
  String currency
) {
}
