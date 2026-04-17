package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.incognia.api.ProjectVersion;
import com.incognia.common.exceptions.IncogniaAPIException;
import com.incognia.common.exceptions.IncogniaException;
import com.incognia.fixtures.TestRequestBody;
import com.incognia.fixtures.TestResponseBody;
import java.io.IOException;
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
  private MockWebServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    client =
        new TokenAwareNetworkingClient(
            new OkHttpClient(), mockServer.url("").toString(), CLIENT_ID, CLIENT_SECRET);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockServer.shutdown();
  }

  @Test
  @DisplayName("should call the api with the same valid token")
  void testDoPost_whenNoTokenExistsYetAndCredentialsAreValid()
      throws IncogniaException, InterruptedException {

    synchronized (this) {
      wait(15000); // Wait for token to expire (15 seconds)
    }

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
}
