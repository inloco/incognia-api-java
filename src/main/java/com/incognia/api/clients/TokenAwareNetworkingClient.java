package com.incognia.api.clients;

import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.OkHttpClient;

public class TokenAwareNetworkingClient {
  private static final String USER_AGENT_HEADER = "User-Agent";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String LATENCY_HEADER = "X-Incognia-Latency";
  private static final String USER_AGENT_HEADER_CONTENT =
      String.format(
          "incognia-java/%s (%s %s %s) Java/%s",
          com.incognia.api.ProjectVersion.PROJECT_VERSION,
          System.getProperty("os.name"),
          System.getProperty("os.version"),
          System.getProperty("os.arch"),
          System.getProperty("java.version"));

  private final NetworkingClient networkingClient;
  private final TokenProvider tokenProvider;
  private final AtomicReference<Long> lastLatency = new AtomicReference<>();

  public TokenAwareNetworkingClient(
      OkHttpClient httpClient, String baseUrl, String clientId, String clientSecret) {
    this(
        httpClient,
        baseUrl,
        new AutoRefreshTokenProvider(
            clientId, clientSecret, new NetworkingClient(httpClient, baseUrl)));
  }

  public TokenAwareNetworkingClient(
      OkHttpClient httpClient, String baseUrl, TokenProvider tokenProvider) {
    this.networkingClient = new NetworkingClient(httpClient, baseUrl);
    this.tokenProvider = tokenProvider;
  }

  public <T, U> U doPost(
      String path, T body, Class<U> responseType, Map<String, String> queryParameters)
      throws IncogniaException {
    Token token = tokenProvider.getToken();
    long start = System.nanoTime();
    U result =
        networkingClient.doPost(path, body, responseType, buildHeaders(token), queryParameters);
    lastLatency.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    return result;
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType) throws IncogniaException {
    Token token = tokenProvider.getToken();
    long start = System.nanoTime();
    U result = networkingClient.doPost(path, body, responseType, buildHeaders(token));
    lastLatency.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    return result;
  }

  public <T> void doPost(String path, T body, Map<String, String> queryParameters)
      throws IncogniaException {
    Token token = tokenProvider.getToken();
    long start = System.nanoTime();
    networkingClient.doPost(path, body, buildHeaders(token), queryParameters);
    lastLatency.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
  }

  private Map<String, String> buildHeaders(Token token) throws IncogniaException {
    validateToken(token);
    Map<String, String> headers = new HashMap<>();
    headers.put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
    headers.put(AUTHORIZATION_HEADER, token.getTokenType() + " " + token.getAccessToken());
    Long latency = lastLatency.get();
    if (latency != null) {
      headers.put(LATENCY_HEADER, Long.toString(latency));
    }
    return headers;
  }

  private void validateToken(Token token) throws IncogniaException {
    if (token == null) {
      throw new IncogniaException("token provider returned a null token");
    }
    if (token.getTokenType() == null || token.getTokenType().isEmpty()) {
      throw new IncogniaException("token provider returned a token without token type");
    }
    if (token.getAccessToken() == null || token.getAccessToken().isEmpty()) {
      throw new IncogniaException("token provider returned a token without access token");
    }
  }
}
