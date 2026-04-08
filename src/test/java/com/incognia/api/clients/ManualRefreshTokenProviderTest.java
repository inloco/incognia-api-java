package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaException;
import com.incognia.common.exceptions.TokenExpiredException;
import com.incognia.common.exceptions.TokenNotFoundException;
import com.incognia.common.utils.CustomOptions;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class ManualRefreshTokenProviderTest {
  private final String CLIENT_ID = "client-id";
  private final String CLIENT_SECRET = "client-secret";
  private ManualRefreshTokenProvider tokenProvider;
  private MockWebServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    tokenProvider =
        new ManualRefreshTokenProvider(
            CLIENT_ID,
            CLIENT_SECRET,
            new NetworkingClient(new OkHttpClient(), mockServer.url("").toString()));
  }

  @AfterEach
  void tearDown() throws Exception {
    mockServer.shutdown();
  }

  @Test
  void testGetToken_whenTokenWasNotRefreshed_shouldThrowTokenNotFoundException() {
    assertThatThrownBy(() -> tokenProvider.getToken()).isInstanceOf(TokenNotFoundException.class);
  }

  @Test
  void testGetToken_whenTokenIsExpired_shouldThrowTokenExpiredException() throws Exception {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);
    tokenProvider.refresh();
    expireToken(tokenProvider);

    assertThatThrownBy(() -> tokenProvider.getToken()).isInstanceOf(TokenExpiredException.class);
  }

  @Test
  void testRefresh_shouldStoreAndReturnAToken() throws IncogniaException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    Token refreshedToken = tokenProvider.refresh();

    assertThat(tokenProvider.getToken()).isSameAs(refreshedToken);
    assertThat(refreshedToken.getExpiresAt()).isAfter(Instant.now());
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testConstructor_whenCustomOptionsProvided_shouldCreateOkHttpWithRightParameters() {
    AtomicReference<List<Object>> poolArgs = new AtomicReference<>();

    try (MockedConstruction<OkHttpClient.Builder> builderConstruction =
            mockConstruction(
                OkHttpClient.Builder.class,
                (mock, context) -> {
                  doReturn(mock).when(mock).callTimeout(anyLong(), any());
                  doReturn(mock).when(mock).connectionPool(any());
                  doReturn(mock(OkHttpClient.class)).when(mock).build();
                });
        MockedConstruction<ConnectionPool> ignored =
            mockConstruction(
                ConnectionPool.class,
                (mock, context) -> poolArgs.set((List<Object>) context.arguments()))) {
      long timeoutMillis = 1234L;
      int maxConnections = 8;
      long keepAliveSeconds = 45L;

      new ManualRefreshTokenProvider(
          CLIENT_ID,
          CLIENT_SECRET,
          CustomOptions.builder()
              .timeoutMillis(timeoutMillis)
              .maxConnections(maxConnections)
              .keepAliveSeconds(keepAliveSeconds)
              .build());

      verify(builderConstruction.constructed().get(0))
          .callTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
      assertThat(poolArgs.get())
          .containsExactly(maxConnections, keepAliveSeconds, TimeUnit.SECONDS);
    }
  }

  private static void expireToken(ManualRefreshTokenProvider tokenProvider) throws Exception {
    Field tokenField = ManualRefreshTokenProvider.class.getDeclaredField("token");
    tokenField.setAccessible(true);
    Token token = (Token) tokenField.get(tokenProvider);
    tokenField.set(
        tokenProvider, new Token(token.getAccessToken(), token.getTokenType(), Instant.EPOCH));
  }
}
