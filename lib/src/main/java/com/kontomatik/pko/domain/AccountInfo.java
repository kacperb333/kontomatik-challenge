package com.kontomatik.pko.domain;

public record AccountInfo(
    String name,
    Long balance,
    String currency
) {
}
