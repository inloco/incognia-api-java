package com.incognia.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

public class TokenAwareDispatcher extends Dispatcher {
  private final String token;
  private final String clientId;
  private final String clientSecret;
  @Getter private int tokenRequestCount;

  public TokenAwareDispatcher(String token, String clientId, String clientSecret) {
    this.token = token;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.tokenRequestCount = 0;
  }

  @SneakyThrows
  @NotNull
  @Override
  public MockResponse dispatch(@NotNull RecordedRequest request) {
    if ("/api/v2/onboarding".equals(request.getPath()) && "POST".equals(request.getMethod())) {
      assertThat(request.getHeader("Content-Type")).contains("application/json");
      assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
      return new MockResponse().setResponseCode(200).setBody("{\"name\": \"my awesome name\"}");
    }
    if ("/api/v1/token".equals(request.getPath()) && "POST".equals(request.getMethod())) {
      tokenRequestCount++;
      String authorizationHeader = request.getHeader("Authorization");
      assertThat(authorizationHeader).startsWith("Basic");
      assertThat(request.getHeader("Content-Type")).contains("application/x-www-form-urlencoded");
      String[] idAndSecret =
          new String(
                  Base64.getUrlDecoder().decode(authorizationHeader.split(" ")[1]),
                  StandardCharsets.UTF_8)
              .split(":", 2);
      String clientId = idAndSecret[0];
      String clientSecret = idAndSecret[1];
      if (this.clientId.equals(clientId) && this.clientSecret.equals(clientSecret)) {
        return new MockResponse()
            .setResponseCode(200)
            .setBody(
                "{\"access_token\": \""
                    + token
                    + "\",\"expires_in\": 100,\"token_type\": \"Bearer\"}");
      }
      return new MockResponse().setResponseCode(401);
    }
    return new MockResponse().setResponseCode(404);
  }
}
