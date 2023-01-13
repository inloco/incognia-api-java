package com.incognia.onboarding;

import com.incognia.common.Address;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterSignupRequest {
  String installationId;
  Address address;
  String externalId;
  String policyId;
  String accountId;
}
