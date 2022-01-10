package com.incognia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterSignupRequest {
  String installationId;
  Address address;
}
