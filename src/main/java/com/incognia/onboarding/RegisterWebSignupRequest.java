package com.incognia.onboarding;

import lombok.Builder;
import lombok.Value;
import java.util.Map;

@Value
@Builder
public class RegisterWebSignupRequest {
  String sessionToken;
  String requestToken;
  String externalId;
  String policyId;
  String accountId;
  Map<String, Object> customProperties;
}
