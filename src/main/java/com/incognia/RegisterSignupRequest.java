package com.incognia;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class RegisterSignupRequest {
  @NonNull String installationId;
  @NonNull Address address;
}
