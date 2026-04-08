package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoRefreshTokenProviderTest {
  private final String CLIENT_ID = "client-id";
  private final String ANOTHER_CLIENT_ID = "another-client-id";
  private final String CLIENT_SECRET = "client-secret";
  private MockWebServer mockServer;
  private AutoRefreshTokenProvider tokenProvider;
  private AutoRefreshTokenProvider anotherTokenProvider;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    tokenProvider =
        new AutoRefreshTokenProvider(
            CLIENT_ID,
            CLIENT_SECRET,
            new NetworkingClient(new OkHttpClient(), mockServer.url("").toString()));
    anotherTokenProvider =
        new AutoRefreshTokenProvider(
            ANOTHER_CLIENT_ID,
            CLIENT_SECRET,
            new NetworkingClient(new OkHttpClient(), mockServer.url("").toString()));
  }

  @AfterEach
  void tearDown() throws Exception {
    mockServer.shutdown();
  }

  @Test
  void testGetToken_whenTokenIsNull_shouldReturnTheSameTokenForTheSameInstance()
      throws IncogniaException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    Token firstToken = tokenProvider.getToken();
    Token secondToken = tokenProvider.getToken();

    assertThat(firstToken).isSameAs(secondToken);
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  void testGetToken_whenCalledConcurrently_shouldRequestTheTokenOnlyOnce() throws Exception {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);
    int threadCount = 8;
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    try {
      List<Future<Token>> futures = new ArrayList<>();
      for (int i = 0; i < threadCount; i++) {
        futures.add(
            executor.submit(
                () -> {
                  ready.countDown();
                  assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
                  return tokenProvider.getToken();
                }));
      }

      assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
      start.countDown();

      Token firstToken = futures.get(0).get(5, TimeUnit.SECONDS);
      for (Future<Token> future : futures) {
        assertThat(future.get(5, TimeUnit.SECONDS)).isSameAs(firstToken);
      }
    } finally {
      executor.shutdownNow();
    }

    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  void testGetToken_whenDifferentProvidersUseDifferentCredentials_shouldNotShareTokens()
      throws IncogniaException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    TokenAwareDispatcher anotherDispatcher =
        new TokenAwareDispatcher(ANOTHER_CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    Token token = tokenProvider.getToken();

    mockServer.setDispatcher(anotherDispatcher);
    Token anotherToken = anotherTokenProvider.getToken();

    assertThat(anotherToken).isNotSameAs(token);
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
    assertThat(anotherDispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  void testGetToken_whenTokenIsExpired_shouldRefreshToken() throws Exception {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    Token token = tokenProvider.getToken();
    expireToken(tokenProvider, token);

    Token refreshedToken = tokenProvider.getToken();

    assertThat(refreshedToken).isNotSameAs(token);
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(2);
  }

  private static void expireToken(AutoRefreshTokenProvider tokenProvider, Token token)
      throws Exception {
    Field tokenField = AutoRefreshTokenProvider.class.getDeclaredField("token");
    tokenField.setAccessible(true);
    tokenField.set(
        tokenProvider, new Token(token.getAccessToken(), token.getTokenType(), Instant.EPOCH));
  }
}
