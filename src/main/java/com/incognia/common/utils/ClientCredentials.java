package com.incognia.common.utils;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClientCredentials {
  String clientId;
  String clientSecret;
}
