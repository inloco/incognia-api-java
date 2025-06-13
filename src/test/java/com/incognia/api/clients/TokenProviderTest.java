package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.incognia.common.exceptions.IncogniaException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;

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
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    invalidateToken();

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
  void tearDown() throws IOException, NoSuchFieldException, IllegalAccessException {
    mockServer.shutdown();
    invalidateToken();
  }

  @Test
  public void testGetToken_whenTokenIsNull_shouldReturnTokenForAllInstances()
      throws IncogniaException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);
    tokenProvider.getToken();
    String authorizationHeader = tokenProvider.buildAuthorizationHeader();
    assertThat(authorizationHeader).isNotEmpty();

    String anotherAuthorizationHeader = anotherTokenProvider.buildAuthorizationHeader();
    assertThat(anotherAuthorizationHeader).isEqualTo(authorizationHeader);
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  public void testGetToken_whenTokenIsNotNullAndExpired_shouldUpdateTokenForAllInstances()
      throws IncogniaException, NoSuchFieldException, IllegalAccessException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    TokenAwareDispatcher anotherDispatcher =
        new TokenAwareDispatcher(ANOTHER_CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    TokenResponse token = tokenProvider.getToken();
    assertThat(token).isNotNull();

    invalidateToken();

    mockServer.setDispatcher(anotherDispatcher);
    TokenResponse newToken = anotherTokenProvider.getToken();

    mockServer.setDispatcher(dispatcher);
    token = tokenProvider.getToken();

    assertThat(newToken).isNotNull();
    assertThat(newToken).isEqualTo(token);
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
    assertThat(anotherDispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  private static void invalidateToken() throws NoSuchFieldException, IllegalAccessException {
    Field tokenField = TokenProvider.class.getDeclaredField("token");
    tokenField.setAccessible(true);
    TokenResponse token = (TokenResponse) tokenField.get(null);

    if (token == null) {
      return;
    }

    TokenResponse expiredToken = new TokenResponse(token.getAccessToken(),
        token.getExpiresIn(),
        token.getTokenType(),
        Instant.EPOCH);

    tokenField.set(null, expiredToken);

  }
}
