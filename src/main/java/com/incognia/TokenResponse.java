package com.incognia;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
  private String accessToken;
  private long expiresIn;
  private String tokenType;
}
