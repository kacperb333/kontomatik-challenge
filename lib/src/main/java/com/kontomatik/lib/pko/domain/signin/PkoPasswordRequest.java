package com.kontomatik.lib.pko.domain.signin;

record PkoPasswordRequest(
  Integer version,
  String state_id,
  String action,
  String flow_id,
  String token,
  PkoPasswordData data
) {
  static final int REQUEST_VERSION = 3;

  static PkoPasswordRequest newRequest(
    String password,
    OtpRequiredPkoSession otpRequiredPkoSession
  ) {
    return new PkoPasswordRequest(
      REQUEST_VERSION,
      "password",
      "submit",
      otpRequiredPkoSession.flowId().value(),
      otpRequiredPkoSession.token().value(),
      new PkoPasswordData(password)
    );
  }

  record PkoPasswordData(
    String password
  ) {
  }
}
