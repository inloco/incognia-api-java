package com.incognia.common.utils;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CustomOptions {
  @Builder.Default long timeoutMillis = 10000L;
  @Builder.Default int maxConnections = 5;
  @Builder.Default long keepAliveSeconds = 300;
}
