package com.incognia.onboarding;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterWebSignupRequest {
  String sessionToken;
  String requestToken;
  String externalId;
  String policyId;
  String accountId;
}
