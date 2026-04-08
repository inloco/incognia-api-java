package com.incognia.api.clients;

import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaException;
import com.incognia.common.exceptions.TokenExpiredException;
import com.incognia.common.exceptions.TokenNotFoundException;
import com.incognia.common.utils.Asserts;
import com.incognia.common.utils.CustomOptions;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

public class ManualRefreshTokenProvider implements TokenProvider {
  private static final String API_URL = "https://api.incognia.com";

  private final ReentrantLock lock = new ReentrantLock();
  private final TokenRequester tokenRequester;
  private volatile Token token;

  public ManualRefreshTokenProvider(String clientId, String clientSecret) {
    this(clientId, clientSecret, CustomOptions.builder().build());
  }

  public ManualRefreshTokenProvider(String clientId, String clientSecret, CustomOptions options) {
    this(clientId, clientSecret, createNetworkingClient(options));
  }

  ManualRefreshTokenProvider(
      String clientId, String clientSecret, NetworkingClient networkingClient) {
    Asserts.assertNotEmpty(clientId, "client id");
    Asserts.assertNotEmpty(clientSecret, "client secret");
    Asserts.assertNotNull(networkingClient, "networking client");
    this.tokenRequester = new TokenRequester(clientId, clientSecret, networkingClient);
  }

  @Override
  public Token getToken() throws IncogniaException {
    Token currentToken = token;
    if (currentToken == null) {
      throw new TokenNotFoundException();
    }

    if (currentToken.isExpired()) {
      throw new TokenExpiredException();
    }

    return currentToken;
  }

  public Token refresh() throws IncogniaException {
    lock.lock();
    try {
      token = tokenRequester.requestToken();
      return token;
    } finally {
      lock.unlock();
    }
  }

  private static NetworkingClient createNetworkingClient(CustomOptions options) {
    Asserts.assertNotNull(options, "custom options");
    OkHttpClient httpClient =
        new OkHttpClient.Builder()
            .callTimeout(options.getTimeoutMillis(), TimeUnit.MILLISECONDS)
            .connectionPool(
                new ConnectionPool(
                    options.getMaxConnections(), options.getKeepAliveSeconds(), TimeUnit.SECONDS))
            .build();
    return new NetworkingClient(httpClient, API_URL);
  }
}
