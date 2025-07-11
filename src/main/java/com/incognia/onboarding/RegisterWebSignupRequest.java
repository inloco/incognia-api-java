package com.incognia.onboarding;

import java.util.Map;
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
  Map<String, Object> customProperties;
}
