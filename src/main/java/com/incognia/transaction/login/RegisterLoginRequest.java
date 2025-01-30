package com.incognia.transaction.login;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class RegisterLoginRequest {
  String installationId;
  String requestToken;
  String accountId;
  String externalId;
  String policyId;
  String appVersion;
  String deviceOs;
  Map<String, Object> customProperties;

  @Getter(AccessLevel.NONE)
  Boolean evaluateTransaction;

  public Boolean shouldEvaluateTransaction() {
    return this.evaluateTransaction;
  }
}
