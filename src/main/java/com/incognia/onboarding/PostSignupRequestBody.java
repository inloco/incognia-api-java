package com.incognia.onboarding;

import com.incognia.common.AdditionalLocation;
import com.incognia.common.Coordinates;
import com.incognia.common.StructuredAddress;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostSignupRequestBody {
  String installationId;
  String addressLine;
  StructuredAddress structuredAddress;
  Coordinates addressCoordinates;
  String externalId;
  String policyId;
  String accountId;
  List<AdditionalLocation> additionalLocations;
}
