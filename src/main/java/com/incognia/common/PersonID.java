package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonID {
  String type;
  String value;
}
