package com.kontomatik.lib.pko.domain.login;

record PkoLoginRequest(
  Integer version,
  String state_id,
  String action,
  PkoLoginData data
) {
  static final int REQUEST_VERSION = 3;

  static PkoLoginRequest newRequest(String login) {
    return new PkoLoginRequest(
      REQUEST_VERSION,
      "login",
      "submit",
      new PkoLoginData(login)
    );
  }

  record PkoLoginData(
    String login
  ) {
  }
}
