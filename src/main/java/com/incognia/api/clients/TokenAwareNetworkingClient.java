package com.incognia.api.clients;

import com.incognia.common.exceptions.IncogniaException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;

public class TokenAwareNetworkingClient {
  private static final String USER_AGENT_HEADER = "User-Agent";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String USER_AGENT_HEADER_CONTENT =
      String.format(
          "incognia-api-java/%s (%s %s %s) Java/%s",
          com.incognia.api.ProjectVersion.PROJECT_VERSION,
          System.getProperty("os.name"),
          System.getProperty("os.version"),
          System.getProperty("os.arch"),
          System.getProperty("java.version"));

  private final NetworkingClient networkingClient;
  private final TokenProvider tokenProvider;

  public TokenAwareNetworkingClient(
      OkHttpClient httpClient, String baseUrl, String clientId, String clientSecret) {
    this.networkingClient = new NetworkingClient(httpClient, baseUrl);
    this.tokenProvider = new TokenProvider(clientId, clientSecret, networkingClient);
  }

  public <T, U> U doPost(
      String path, T body, Class<U> responseType, Map<String, String> queryParameters)
      throws IncogniaException {
    tokenProvider.getToken();
    Map<String, String> headers =
        new HashMap<String, String>() {
          {
            put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
            put(AUTHORIZATION_HEADER, tokenProvider.buildAuthorizationHeader());
          }
        };
    return networkingClient.doPost(path, body, responseType, headers, queryParameters);
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType) throws IncogniaException {
    tokenProvider.getToken();
    Map<String, String> headers =
        new HashMap<String, String>() {
          {
            put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
            put(AUTHORIZATION_HEADER, tokenProvider.buildAuthorizationHeader());
          }
        };
    return networkingClient.doPost(path, body, responseType, headers);
  }

  public <T> void doPost(String path, T body, Map<String, String> queryParameters)
      throws IncogniaException {
    tokenProvider.getToken();
    Map<String, String> headers =
        new HashMap<String, String>() {
          {
            put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
            put(AUTHORIZATION_HEADER, tokenProvider.buildAuthorizationHeader());
          }
        };
    networkingClient.doPost(path, body, headers, queryParameters);
  }
}
