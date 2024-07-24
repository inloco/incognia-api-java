package com.incognia.api;

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
class PostSignupRequestBody {
  String installationId;
  String sessionToken;
  String addressLine;
  StructuredAddress structuredAddress;
  Coordinates addressCoordinates;
  String externalId;
  String policyId;
  String accountId;
  List<AdditionalLocation> additionalLocations;
}
