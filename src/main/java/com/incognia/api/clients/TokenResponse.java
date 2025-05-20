package com.incognia.api.clients;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
  private String accessToken;
  private long expiresIn;
  private String tokenType;
  private Instant expiresAt;

  public void setExpiresAt() {
    this.expiresAt = Instant.now().plusSeconds(this.expiresIn);
  }
}
