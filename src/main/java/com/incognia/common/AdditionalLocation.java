package com.incognia.common;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdditionalLocation {
  Double lat;
  Double lng;
  Instant collectedAt;
}
