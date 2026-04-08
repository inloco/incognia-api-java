package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.incognia.api.ProjectVersion;
import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaAPIException;
import com.incognia.common.exceptions.IncogniaException;
import com.incognia.common.exceptions.TokenExpiredException;
import com.incognia.common.exceptions.TokenNotFoundException;
import com.incognia.fixtures.TestRequestBody;
import com.incognia.fixtures.TestResponseBody;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenAwareNetworkingClientTest {
  private final String CLIENT_ID = "client-id";
  private final String CLIENT_SECRET = "client-secret";
  private TokenAwareNetworkingClient client;
  private ManualRefreshTokenProvider manualRefreshTokenProvider;
  private TokenAwareNetworkingClient manualClient;
  private MockWebServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    OkHttpClient httpClient = new OkHttpClient();
    client =
        new TokenAwareNetworkingClient(
            httpClient, mockServer.url("").toString(), CLIENT_ID, CLIENT_SECRET);
    manualRefreshTokenProvider =
        new ManualRefreshTokenProvider(
            CLIENT_ID,
            CLIENT_SECRET,
            new NetworkingClient(httpClient, mockServer.url("").toString()));
    manualClient =
        new TokenAwareNetworkingClient(
            httpClient, mockServer.url("").toString(), manualRefreshTokenProvider);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockServer.shutdown();
  }

  @Test
  @DisplayName("should call the api with the same valid token")
  void testDoPost_whenNoTokenExistsYetAndCredentialsAreValid() throws IncogniaException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    for (int i = 0; i < 2; i++) {
      TestResponseBody testResponseBody =
          client.doPost(
              "api/v2/onboarding", new TestRequestBody("my-id", 1234), TestResponseBody.class);
      assertThat(testResponseBody.getName()).isEqualTo("my awesome name");
    }

    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("should use incognia-java user agent without version prefix")
  void testDoPost_whenSendingRequest_shouldUseUpdatedUserAgent()
      throws IncogniaException, InterruptedException {
    mockServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(
                "{\"access_token\": \"token\",\"expires_in\": 12,\"token_type\": \"Bearer\"}"));
    mockServer.enqueue(
        new MockResponse().setResponseCode(200).setBody("{\"name\": \"my awesome name\"}"));

    client.doPost("api/v2/onboarding", new TestRequestBody("my-id", 1234), TestResponseBody.class);

    mockServer.takeRequest();
    RecordedRequest apiRequest = mockServer.takeRequest();
    String userAgent = apiRequest.getHeader("User-Agent");

    assertThat(userAgent).startsWith("incognia-java/" + ProjectVersion.PROJECT_VERSION);
    assertThat(userAgent).doesNotContain("incognia-api-java");
    assertThat(userAgent).doesNotContain("/v" + ProjectVersion.PROJECT_VERSION);
  }

  @Test
  @DisplayName("should use a manually refreshed token without refreshing during the API call")
  void testDoPost_whenUsingManualRefreshProvider_shouldReuseTheCachedToken()
      throws IncogniaException {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);
    manualRefreshTokenProvider.refresh();

    TestResponseBody testResponseBody =
        manualClient.doPost(
            "api/v2/onboarding", new TestRequestBody("my-id", 1234), TestResponseBody.class);

    assertThat(testResponseBody.getName()).isEqualTo("my awesome name");
    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("should get a 401 error")
  void testDoPost_whenNoTokenExistsYetAndCredentialsAreInvalid() {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher("invalid", CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    assertThatThrownBy(
            () ->
                client.doPost(
                    "api/v2/onboarding",
                    new TestRequestBody("my-id", 1234),
                    TestResponseBody.class))
        .satisfies(
            e ->
                assertThat(e)
                    .isInstanceOf(IncogniaAPIException.class)
                    .extracting(IncogniaAPIException.class::cast)
                    .extracting(IncogniaAPIException::getStatusCode)
                    .isEqualTo(401));
  }

  @Test
  @DisplayName("should fail before issuing the API call when no manual token exists")
  void testDoPost_whenUsingManualRefreshProviderWithoutAToken_shouldFailFast() {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);

    assertThatThrownBy(
            () ->
                manualClient.doPost(
                    "api/v2/onboarding",
                    new TestRequestBody("my-id", 1234),
                    TestResponseBody.class))
        .isInstanceOf(TokenNotFoundException.class);

    assertThat(dispatcher.getTokenRequestCount()).isZero();
  }

  @Test
  @DisplayName("should fail before issuing the API call when the manual token is expired")
  void testDoPost_whenUsingManualRefreshProviderWithExpiredToken_shouldFailFast() throws Exception {
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    mockServer.setDispatcher(dispatcher);
    manualRefreshTokenProvider.refresh();

    Field tokenField = ManualRefreshTokenProvider.class.getDeclaredField("token");
    tokenField.setAccessible(true);
    Token token = (Token) tokenField.get(manualRefreshTokenProvider);
    tokenField.set(
        manualRefreshTokenProvider,
        new Token(token.getAccessToken(), token.getTokenType(), Instant.EPOCH));

    assertThatThrownBy(
            () ->
                manualClient.doPost(
                    "api/v2/onboarding",
                    new TestRequestBody("my-id", 1234),
                    TestResponseBody.class))
        .isInstanceOf(TokenExpiredException.class);

    assertThat(dispatcher.getTokenRequestCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("should fail with a clear message when custom token provider returns null")
  void testDoPost_whenCustomTokenProviderReturnsNull_shouldFailWithClearMessage()
      throws IncogniaException {
    TokenProvider tokenProvider = mock(TokenProvider.class);
    doReturn(null).when(tokenProvider).getToken();
    TokenAwareNetworkingClient clientWithInvalidTokenProvider =
        new TokenAwareNetworkingClient(
            new OkHttpClient(), mockServer.url("").toString(), tokenProvider);

    assertThatThrownBy(
            () ->
                clientWithInvalidTokenProvider.doPost(
                    "api/v2/onboarding",
                    new TestRequestBody("my-id", 1234),
                    TestResponseBody.class))
        .isInstanceOf(IncogniaException.class)
        .hasMessage("token provider returned a null token");
  }

  @Test
  @DisplayName("should fail with a clear message when custom token provider returns no token type")
  void testDoPost_whenCustomTokenProviderReturnsTokenWithoutType_shouldFailWithClearMessage()
      throws IncogniaException {
    TokenProvider tokenProvider = mock(TokenProvider.class);
    doReturn(new Token("token", "", Instant.now().plusSeconds(10))).when(tokenProvider).getToken();
    TokenAwareNetworkingClient clientWithInvalidTokenProvider =
        new TokenAwareNetworkingClient(
            new OkHttpClient(), mockServer.url("").toString(), tokenProvider);

    assertThatThrownBy(
            () ->
                clientWithInvalidTokenProvider.doPost(
                    "api/v2/onboarding",
                    new TestRequestBody("my-id", 1234),
                    TestResponseBody.class))
        .isInstanceOf(IncogniaException.class)
        .hasMessage("token provider returned a token without token type");
  }

  @Test
  @DisplayName(
      "should fail with a clear message when custom token provider returns no access token")
  void testDoPost_whenCustomTokenProviderReturnsTokenWithoutAccessToken_shouldFailWithClearMessage()
      throws IncogniaException {
    TokenProvider tokenProvider = mock(TokenProvider.class);
    doReturn(new Token("", "Bearer", Instant.now().plusSeconds(10))).when(tokenProvider).getToken();
    TokenAwareNetworkingClient clientWithInvalidTokenProvider =
        new TokenAwareNetworkingClient(
            new OkHttpClient(), mockServer.url("").toString(), tokenProvider);

    assertThatThrownBy(
            () ->
                clientWithInvalidTokenProvider.doPost(
                    "api/v2/onboarding",
                    new TestRequestBody("my-id", 1234),
                    TestResponseBody.class))
        .isInstanceOf(IncogniaException.class)
        .hasMessage("token provider returned a token without access token");
  }
}
