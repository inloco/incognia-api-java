package com.incognia.transaction.login;

import com.incognia.common.PersonID;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class RegisterWebLoginRequest {
  String accountId;
  String externalId;
  String sessionToken;
  String requestToken;
  String policyId;
  Map<String, Object> customProperties;
  PersonID personId;

  @Getter(AccessLevel.NONE)
  Boolean evaluateTransaction;

  public Boolean shouldEvaluateTransaction() {
    return this.evaluateTransaction;
  }
}
