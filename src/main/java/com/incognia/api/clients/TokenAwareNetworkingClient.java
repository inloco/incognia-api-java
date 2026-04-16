package com.incognia.api.clients;

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
          "incognia-api-java/%s (%s %s %s) Java/%s",
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
    this.networkingClient = new NetworkingClient(httpClient, baseUrl);
    this.tokenProvider = new TokenProvider(clientId, clientSecret, networkingClient);
  }

  private Map<String, String> buildHeaders() throws IncogniaException {
    Map<String, String> headers = new HashMap<>();
    headers.put(USER_AGENT_HEADER, USER_AGENT_HEADER_CONTENT);
    headers.put(AUTHORIZATION_HEADER, tokenProvider.buildAuthorizationHeader());
    Long latency = lastLatency.get();
    if (latency != null) {
      headers.put(LATENCY_HEADER, Long.toString(latency));
    }
    return headers;
  }

  public <T, U> U doPost(
      String path, T body, Class<U> responseType, Map<String, String> queryParameters)
      throws IncogniaException {
    tokenProvider.getToken();
    long start = System.nanoTime();
    U result = networkingClient.doPost(path, body, responseType, buildHeaders(), queryParameters);
    lastLatency.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    return result;
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType) throws IncogniaException {
    tokenProvider.getToken();
    long start = System.nanoTime();
    U result = networkingClient.doPost(path, body, responseType, buildHeaders());
    lastLatency.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    return result;
  }

  public <T> void doPost(String path, T body, Map<String, String> queryParameters)
      throws IncogniaException {
    tokenProvider.getToken();
    long start = System.nanoTime();
    networkingClient.doPost(path, body, buildHeaders(), queryParameters);
    lastLatency.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
  }
}
