package com.kontomatik.pko.domain;

public record Credentials(
    String login,
    char[] password
) {
}
