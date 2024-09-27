package com.incognia.onboarding;

import com.incognia.common.AdditionalLocation;
import com.incognia.common.Coordinates;
import com.incognia.common.StructuredAddress;
import java.util.List;
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
  StructuredAddress structuredAddress;
  Coordinates addressCoordinates;
  String externalId;
  String policyId;
  String accountId;
  List<AdditionalLocation> additionalLocations;
}
