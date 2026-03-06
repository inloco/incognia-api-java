package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PixKey {
  String type;
  String value;
}