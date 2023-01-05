package com.kontomatik.pko.domain;

public record AccountInfo(
    String name,
    String balance,
    String currency
) {
}
