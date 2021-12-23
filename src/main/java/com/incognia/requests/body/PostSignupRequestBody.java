package com.incognia.requests.body;

import com.incognia.requests.Coordinates;
import com.incognia.requests.StructuredAddress;
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
