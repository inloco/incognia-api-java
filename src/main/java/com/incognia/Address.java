package com.incognia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Address {
  String addressLine;
  StructuredAddress structuredAddress;
  Coordinates coordinates;
}
