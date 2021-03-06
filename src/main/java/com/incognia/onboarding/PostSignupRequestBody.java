package com.incognia.onboarding;

import com.incognia.common.Coordinates;
import com.incognia.common.StructuredAddress;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostSignupRequestBody {
  String installationId;
  String addressLine;
  StructuredAddress structuredAddress;
  Coordinates addressCoordinates;
}
