package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.incognia.common.exceptions.IncogniaException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenProviderTest {
  private final String CLIENT_ID = "client-id";
  private final String CLIENT_SECRET = "client-secret";
  private final String DIFFERENT_CLIENT_ID = "different-client-id";
  private final String DIFFERENT_CLIENT_SECRET = "different-client-secret";
  private MockWebServer mockServer;
  private TokenProvider tokenProvider;
  private TokenProvider differentTokenProvider;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    tokenProvider =
        new TokenProvider(
            CLIENT_ID,
            CLIENT_SECRET,
            new NetworkingClient(new OkHttpClient(), mockServer.url("").toString()));
    differentTokenProvider =
        new TokenProvider(
            DIFFERENT_CLIENT_ID,
            DIFFERENT_CLIENT_SECRET,
            new NetworkingClient(new OkHttpClient(), mockServer.url("").toString()));
  }

  @AfterEach
  void tearDown() throws IOException {
    mockServer.shutdown();
  }

  @Test
  public void testGetToken_whenTokenIsNotNullAndExpired_shouldUpdateToken()
      throws IncogniaException, InterruptedException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    TokenResponse token = tokenProvider.getToken();
    assertThat(token).isNotNull();

    synchronized (this) {
      wait(15000); // Wait for token to expire (15 seconds)
    }

    tokenProvider.getToken();

    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(2);
  }

  @Test
  public void testGetToken_whenTokenIsNotExpired_shouldNotUpdateToken()
      throws IncogniaException, InterruptedException {
    synchronized (this) {
      wait(15000); // Wait for token to expire (15 seconds)
    }
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    TokenResponse token = tokenProvider.getToken();
    assertThat(token).isNotNull();

    tokenProvider.getToken();

    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  public void testGetToken_whenMultipleCredentialsExists_shouldReturnDifferentTokens()
      throws IncogniaException, InterruptedException {
    synchronized (this) {
      wait(15000); // Wait for token to expire (15 seconds)
    }
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    TokenResponse token = tokenProvider.getToken();
    assertThat(token).isNotNull();

    TokenAwareDispatcher dispatcher2 =
        new TokenAwareDispatcher(DIFFERENT_CLIENT_ID, DIFFERENT_CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher2);

    TokenResponse anotherToken = differentTokenProvider.getToken();
    assertThat(token).isNotNull();
    assertThat(anotherToken).isNotSameAs(token);
  }

  @Test
  public void testGetToken_whenCalledConcurrent_shouldCallDispatcherOnlyOnce()
      throws InterruptedException {
    synchronized (this) {
      wait(15000); // Wait for token to expire (15 seconds)
    }
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    int numThreads = 10;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    for (int i = 0; i < numThreads; i++) {
      executor.submit(() -> tokenProvider.getToken());
    }

    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }
}
