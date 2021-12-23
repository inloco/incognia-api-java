package com.incognia.requests;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class RegisterLoginRequest {
  String installationId;
  String accountId;
  String externalId;

  @Getter(AccessLevel.NONE)
  Boolean evaluateTransaction;

  public Boolean shouldEvaluateTransaction() {
    return this.evaluateTransaction;
  }
}
