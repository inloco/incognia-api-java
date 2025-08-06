package com.incognia.onboarding;

import com.incognia.common.AdditionalLocation;
import com.incognia.common.Address;
import com.incognia.common.PersonID;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
@Builder
public class RegisterSignupRequest {
  String installationId;
  String requestToken;
  String appVersion;
  String deviceOs;
  @Nullable Address address;
  String externalId;
  String policyId;
  String accountId;
  List<AdditionalLocation> additionalLocations;
  Map<String, Object> customProperties;
  PersonID personId;
}
