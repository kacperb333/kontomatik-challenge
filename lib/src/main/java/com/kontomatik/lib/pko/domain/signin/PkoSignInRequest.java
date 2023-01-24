package com.kontomatik.lib.pko.domain.signin;

record PkoSignInRequest(
  Integer version,
  String state_id,
  String action,
  PkoLoginData data
) {
  static final int REQUEST_VERSION = 3;

  static PkoSignInRequest newRequest(String login) {
    return new PkoSignInRequest(
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
