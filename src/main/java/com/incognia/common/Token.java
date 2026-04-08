package com.incognia.common;

import java.time.Instant;
import lombok.Value;

@Value
public class Token {
  String accessToken;
  String tokenType;
  Instant expiresAt;

  public boolean isExpired() {
    return !expiresAt.isAfter(Instant.now());
  }
}
