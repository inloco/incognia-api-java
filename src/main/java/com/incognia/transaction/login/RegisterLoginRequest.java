package com.incognia.transaction.login;

import com.incognia.common.Location;
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
  String relatedAccountId;
  Map<String, Object> customProperties;
  Location location;

  @Getter(AccessLevel.NONE)
  Boolean evaluateTransaction;

  public Boolean shouldEvaluateTransaction() {
    return this.evaluateTransaction;
  }
}
