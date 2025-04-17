package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Location {
  String latitude;
  String longitude;
  String collectedAt;
}
