package com.incognia.api.clients;

import com.incognia.common.exceptions.IncogniaException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;

public class TokenAwareNetworkingClient {
  private static final String TOKEN_PATH = "api/v2/token";
  private static final String USER_AGENT_HEADER = "User-Agent";
  private static final String USER_AGENT_HEADER_CONTENT = buildUserAgentHeader();
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final int TOKEN_REFRESH_BEFORE_SECONDS = 10;
  private static final String TOKEN_REQUEST_BODY = "grant_type=client_credentials";

  private final NetworkingClient networkingClient;
  private final String clientId;
  private final String clientSecret;
  private TokenResponse token;
  private Instant tokenExpiration;

  public TokenAwareNetworkingClient(
      OkHttpClient httpClient, String baseUrl, String clientId, String clientSecret) {
    this.networkingClient = new NetworkingClient(httpClient, baseUrl);
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.token = null;
    this.tokenExpiration = null;
  }

  public <T, U> U doPost(
      String path, T body, Class<U> responseType, Map<String, String> queryParameters)
      throws IncogniaException {
    refreshTokenIfNeeded();
    Map<String, String> headers =
        new HashMap<String, String>() {
          {
            put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
            put(AUTHORIZATION_HEADER, buildAuthorizationHeader());
          }
        };
    return networkingClient.doPost(path, body, responseType, headers, queryParameters);
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType) throws IncogniaException {
    refreshTokenIfNeeded();
    Map<String, String> headers =
        new HashMap<String, String>() {
          {
            put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
            put(AUTHORIZATION_HEADER, buildAuthorizationHeader());
          }
        };
    return networkingClient.doPost(path, body, responseType, headers);
  }

  public <T> void doPost(String path, T body, Map<String, String> queryParameters)
      throws IncogniaException {
    refreshTokenIfNeeded();
    Map<String, String> headers =
        new HashMap<String, String>() {
          {
            put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
            put(AUTHORIZATION_HEADER, buildAuthorizationHeader());
          }
        };
    networkingClient.doPost(path, body, headers, queryParameters);
  }

  public <T> T doGet(String path, Class<T> responseType) throws IncogniaException {
    refreshTokenIfNeeded();
    Map<String, String> headers =
        new HashMap<String, String>() {
          {
            put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
            put(AUTHORIZATION_HEADER, buildAuthorizationHeader());
          }
        };
    return networkingClient.doGet(path, responseType, headers);
  }

  private String buildAuthorizationHeader() {
    return token.getTokenType() + " " + token.getAccessToken();
  }

  private static String buildUserAgentHeader() {
    return String.format(
        "incognia-api-java/%s (%s %s %s) Java/%s",
        com.incognia.api.ProjectVersion.PROJECT_VERSION,
        System.getProperty("os.name"),
        System.getProperty("os.version"),
        System.getProperty("os.arch"),
        System.getProperty("java.version"));
  }

  private void refreshTokenIfNeeded() throws IncogniaException {
    if (token == null
        || Instant.now().until(tokenExpiration, ChronoUnit.SECONDS)
            <= TOKEN_REFRESH_BEFORE_SECONDS) {
      // TODO(rato): handle concurrent requests
      token = getNewToken();
      tokenExpiration = Instant.now().plusSeconds(token.getExpiresIn());
    }
  }

  private TokenResponse getNewToken() throws IncogniaException {
    String clientIdSecret = clientId + ":" + clientSecret;
    Map<String, String> headers =
        Collections.singletonMap(
            AUTHORIZATION_HEADER,
            "Basic " + Base64.getUrlEncoder().encodeToString(clientIdSecret.getBytes()));
    return networkingClient.doPostFormUrlEncoded(
        TOKEN_PATH, TOKEN_REQUEST_BODY, TokenResponse.class, headers);
  }
}
