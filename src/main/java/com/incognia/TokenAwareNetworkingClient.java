package com.incognia;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;

public class TokenAwareNetworkingClient {
  private static final String TOKEN_PATH = "api/v1/token";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final int TOKEN_REFRESH_BEFORE_SECONDS = 10;

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
    refreshTokenIfNeeded();
    return networkingClient.doPost(
        path,
        body,
        responseType,
        Collections.singletonMap(AUTHORIZATION_HEADER, "Bearer " + token.getToken()));
  }

  public <T> void doPost(String path, T body) throws IncogniaException {
    refreshTokenIfNeeded();
    networkingClient.doPost(
        path, body, Collections.singletonMap(AUTHORIZATION_HEADER, "Bearer " + token.getToken()));
  }

  public <T> T doGet(String path, Class<T> responseType) throws IncogniaException {
    refreshTokenIfNeeded();
    return networkingClient.doGet(
        path,
        responseType,
        Collections.singletonMap(AUTHORIZATION_HEADER, "Bearer " + token.getToken()));
  }

  private void refreshTokenIfNeeded() throws IncogniaException {
    if (token == null
        || Instant.now().until(token.getExpiresAt().toInstant(), ChronoUnit.SECONDS)
            <= TOKEN_REFRESH_BEFORE_SECONDS) {
      // TODO(rato): handle concurrent requests
      token = getNewToken();
    }
  }

  private DecodedJWT getNewToken() throws IncogniaException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");
    String clientIdSecret = clientId + ":" + clientSecret;
    headers.put(
        AUTHORIZATION_HEADER,
        "Basic " + Base64.getUrlEncoder().encodeToString(clientIdSecret.getBytes()));
    TokenResponse tokenResponse = networkingClient.doPost(TOKEN_PATH, TokenResponse.class, headers);
    return JWT.decode(tokenResponse.getAccessToken());
  }
}
