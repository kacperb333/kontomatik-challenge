package com.kontomatik.lib.pko.domain.signin;

record PkoOtpRequest(
  Integer version,
  String state_id,
  String action,
  String flow_id,
  String token,
  PkoOtpData data
) {
  static final int REQUEST_VERSION = 3;

  static PkoOtpRequest newRequest(String otp, OtpRequiredPkoSession signInProgressPkoSession) {
    return new PkoOtpRequest(
      REQUEST_VERSION,
      "one_time_password",
      "submit",
      signInProgressPkoSession.flowId().value(),
      signInProgressPkoSession.token().value(),
      new PkoOtpData(otp)
    );
  }

  record PkoOtpData(
    String otp
  ) {
  }
}
