package com.incognia.api.clients;

import com.incognia.common.exceptions.IncogniaException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class TokenProvider {
  private static final int TOKEN_REFRESH_BEFORE_SECONDS = 10;
  private static final String TOKEN_REQUEST_BODY = "grant_type=client_credentials";
  private static final String TOKEN_PATH = "api/v2/token";

  // This implementation assumes that only one instance of this class
  // is created per IncogniaAPI instance.
  // Therefore, for each (clientId, clientSecret) pair, there is exactly
  // one instance of this class.
  private final ReentrantLock lock = new ReentrantLock();
  private volatile TokenResponse token;

  private final String clientId;
  private final String clientSecret;
  private final NetworkingClient networkingClient;

  public TokenProvider(String clientId, String clientSecret, NetworkingClient networkingClient) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.networkingClient = networkingClient;
  }

  public TokenResponse getToken() throws IncogniaException {
    refreshTokenIfNeeded();
    return token;
  }

  public String buildAuthorizationHeader() throws IncogniaException {
    if (token == null) {
      refreshTokenIfNeeded();
    }

    return token.getTokenType() + " " + token.getAccessToken();
  }

  private boolean needsRefresh() {
    return token == null
        || Instant.now().until(token.getExpiresAt(), ChronoUnit.SECONDS)
            <= TOKEN_REFRESH_BEFORE_SECONDS;
  }

  private void refreshTokenIfNeeded() throws IncogniaException {
    if (needsRefresh()) {
      lock.lock();
      try {
        if (needsRefresh()) {
          token = getNewToken();
          token.computeExpiresAt();
        }
      } finally {
        lock.unlock();
      }
    }
  }

  private TokenResponse getNewToken() throws IncogniaException {
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
}
