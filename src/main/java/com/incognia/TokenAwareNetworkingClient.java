package com.incognia;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;

public class TokenAwareNetworkingClient {
  private final NetworkingClient networkingClient;
  private final String clientId;
  private final String clientSecret;
  private DecodedJWT token;

  public TokenAwareNetworkingClient(
      OkHttpClient httpClient, String baseUrl, String clientId, String clientSecret) {
    this.networkingClient = new NetworkingClient(httpClient, baseUrl);
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.token = null;
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType) throws IncogniaException {
    if (token == null || token.getExpiresAt().toInstant().isAfter(Instant.now().plusSeconds(10))) {
      token = getToken();
    }
    return networkingClient.doPost(
        path,
        body,
        responseType,
        Collections.singletonMap("Authorization", "Bearer " + token.getToken()));
  }

  private DecodedJWT getToken() throws IncogniaException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");
    String clientIdSecret = clientId + ":" + clientSecret;
    headers.put(
        "Authorization",
        "Basic " + Base64.getUrlEncoder().encodeToString(clientIdSecret.getBytes()));
    TokenResponse tokenResponse =
        networkingClient.doPost("api/v1/token", null, TokenResponse.class, headers);
    return JWT.decode(tokenResponse.getAccessToken());
  }
}
