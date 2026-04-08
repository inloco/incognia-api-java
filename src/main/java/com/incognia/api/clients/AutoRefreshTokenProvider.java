package com.incognia.api.clients;

import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AutoRefreshTokenProvider implements TokenProvider {
  private static final int TOKEN_REFRESH_BEFORE_SECONDS = 10;

  private final ReentrantLock lock = new ReentrantLock();
  private final TokenRequester tokenRequester;
  private volatile Token token;

  public AutoRefreshTokenProvider(
      String clientId, String clientSecret, NetworkingClient networkingClient) {
    this.tokenRequester = new TokenRequester(clientId, clientSecret, networkingClient);
  }

  @Override
  public Token getToken() throws IncogniaException {
    refreshTokenIfNeeded();
    return token;
  }

  private boolean needsRefresh(Token token) {
    return token == null
        || Instant.now().until(token.getExpiresAt(), ChronoUnit.SECONDS)
            <= TOKEN_REFRESH_BEFORE_SECONDS;
  }

  private void refreshTokenIfNeeded() throws IncogniaException {
    Token currentToken = token;
    if (needsRefresh(currentToken)) {
      lock.lock();
      try {
        if (needsRefresh(token)) {
          token = tokenRequester.requestToken();
        }
      } finally {
        lock.unlock();
      }
    }
  }
}
