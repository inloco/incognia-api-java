package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Reason {
  String code;
  String source;
}
