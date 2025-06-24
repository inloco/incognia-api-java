package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.incognia.common.exceptions.IncogniaException;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenProviderTest {
  private final String CLIENT_ID = "client-id";
  private final String CLIENT_SECRET = "client-secret";
  private MockWebServer mockServer;
  private TokenProvider tokenProvider;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    tokenProvider =
        new TokenProvider(
            CLIENT_ID,
            CLIENT_SECRET,
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
}
