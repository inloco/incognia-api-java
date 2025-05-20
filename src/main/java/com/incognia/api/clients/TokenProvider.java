package com.incognia.api.clients;

import com.incognia.common.exceptions.IncogniaException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TokenProvider {
  private static final int TOKEN_REFRESH_BEFORE_SECONDS = 10;
  private static final String TOKEN_REQUEST_BODY = "grant_type=client_credentials";
  private static final String TOKEN_PATH = "api/v2/token";

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private static volatile TokenResponse token;
  private static volatile Instant tokenExpiration;

  private final String clientId;
  private final String clientSecret;
  private final NetworkingClient networkingClient;

  public TokenProvider(String clientId, String clientSecret, NetworkingClient networkingClient) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.networkingClient = networkingClient;
  }

  public void refreshTokenIfNeeded() throws IncogniaException {
    lock.readLock().lock();
    if (token == null
        || Instant.now().until(tokenExpiration, ChronoUnit.SECONDS)
            <= TOKEN_REFRESH_BEFORE_SECONDS) {
      lock.readLock().unlock();

      TokenResponse newToken = getNewToken();

      lock.writeLock().lock();
      try {
        token = newToken;
        tokenExpiration = Instant.now().plusSeconds(token.getExpiresIn());
      } finally {
        lock.writeLock().unlock();
      }
    } else {
      lock.readLock().unlock();
    }
  }

  public String buildAuthorizationHeader() throws IncogniaException {
    if (token == null) {
      refreshTokenIfNeeded();
    }

    return token.getTokenType() + " " + token.getAccessToken();
  }

  public TokenResponse getNewToken() throws IncogniaException {
    String clientIdSecret = clientId + ":" + clientSecret;
    Map<String, String> headers =
        Collections.singletonMap(
            "Authorization",
            "Basic "
                + Base64.getUrlEncoder()
                    .encodeToString(clientIdSecret.getBytes(StandardCharsets.UTF_8)));
    return networkingClient.doPostFormUrlEncoded(
        TOKEN_PATH, TOKEN_REQUEST_BODY, TokenResponse.class, headers);
  }

  public static void invalidToken() {
    tokenExpiration = Instant.now();
  }

  public static void resetToken() {
    token = null;
  }
}
