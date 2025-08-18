package com.incognia.onboarding;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.incognia.common.AdditionalLocation;
import com.incognia.common.Coordinates;
import com.incognia.common.PersonID;
import com.incognia.common.StructuredAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PostSignupRequestBody {
  String installationId;
  String sessionToken;
  String requestToken;
  String addressLine;
  String appVersion;
  String deviceOs;
  StructuredAddress structuredAddress;
  Coordinates addressCoordinates;
  String externalId;
  String policyId;
  String accountId;
  List<AdditionalLocation> additionalLocations;
  PersonID personId;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  Map<String, Object> customProperties = Collections.emptyMap();
}
