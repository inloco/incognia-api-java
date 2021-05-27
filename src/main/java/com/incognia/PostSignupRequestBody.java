package com.incognia;

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
