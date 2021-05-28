package com.incognia;

import lombok.NonNull;
import lombok.Value;

@Value
public class Coordinates {
  @NonNull Double lat;
  @NonNull Double lng;
}
