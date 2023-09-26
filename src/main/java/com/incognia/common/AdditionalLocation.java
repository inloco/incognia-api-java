package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdditionalLocation {
  Double lat;
  Double lng;
  Long timestamp;
}
