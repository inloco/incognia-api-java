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
  private final String ANOTHER_CLIENT_ID = "another-client-id";
  private final String CLIENT_SECRET = "client-secret";
  private MockWebServer mockServer;
  private TokenProvider tokenProvider;
  private TokenProvider anotherTokenProvider;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    tokenProvider =
        new TokenProvider(
            CLIENT_ID,
            CLIENT_SECRET,
            new NetworkingClient(new OkHttpClient(), mockServer.url("").toString()));
    anotherTokenProvider =
        new TokenProvider(
            ANOTHER_CLIENT_ID,
            CLIENT_SECRET,
            new NetworkingClient(new OkHttpClient(), mockServer.url("").toString()));
  }

  @AfterEach
  void tearDown() throws IOException {
    mockServer.shutdown();
  }

  @Test
  public void testRefreshTokenIfNeeded_whenTokenIsNull_shouldReturnTokenForAllInstances()
      throws IncogniaException {
    TokenProvider.resetToken();
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);
    tokenProvider.refreshTokenIfNeeded();
    String authorizationHeader = tokenProvider.buildAuthorizationHeader();
    assertThat(authorizationHeader).isNotEmpty();

    String anotherAuthorizationHeader = anotherTokenProvider.buildAuthorizationHeader();
    assertThat(anotherAuthorizationHeader).isEqualTo(authorizationHeader);
  }

  @Test
  public void testRefreshTokenIfNeeded_whenTokenIsNotNull_shouldUpdateTokenForAllInstances()
      throws IncogniaException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    TokenAwareDispatcher anotherDispatcher =
        new TokenAwareDispatcher(ANOTHER_CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    tokenProvider.refreshTokenIfNeeded();
    String authorizationHeader = tokenProvider.buildAuthorizationHeader();
    assertThat(authorizationHeader).isNotEmpty();

    TokenProvider.invalidToken();

    mockServer.setDispatcher(anotherDispatcher);
    anotherTokenProvider.refreshTokenIfNeeded();
    String anotherAuthorizationHeader = anotherTokenProvider.buildAuthorizationHeader();
    assertThat(anotherAuthorizationHeader).isNotEqualTo(authorizationHeader);
    assertThat(anotherAuthorizationHeader).isEqualTo(tokenProvider.buildAuthorizationHeader());
  }
}
