package com.incognia.api.clients;

import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

final class TokenRequester {
  private static final String TOKEN_REQUEST_BODY = "grant_type=client_credentials";
  private static final String TOKEN_PATH = "api/v2/token";

  private final String clientId;
  private final String clientSecret;
  private final NetworkingClient networkingClient;

  TokenRequester(String clientId, String clientSecret, NetworkingClient networkingClient) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.networkingClient = networkingClient;
  }

  Token requestToken() throws IncogniaException {
    String clientIdSecret = clientId + ":" + clientSecret;
    Map<String, String> headers =
        Collections.singletonMap(
            "Authorization",
            "Basic "
                + Base64.getEncoder()
                    .encodeToString(clientIdSecret.getBytes(StandardCharsets.UTF_8)));
    TokenResponse tokenResponse =
        networkingClient.doPostFormUrlEncoded(
            TOKEN_PATH, TOKEN_REQUEST_BODY, TokenResponse.class, headers);
    return new Token(
        tokenResponse.getAccessToken(),
        tokenResponse.getTokenType(),
        Instant.now().plusSeconds(tokenResponse.getExpiresIn()));
  }
}
