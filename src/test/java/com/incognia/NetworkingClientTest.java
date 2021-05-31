package com.incognia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.incognia.fixtures.TestRequestBody;
import com.incognia.fixtures.TestResponseBody;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NetworkingClientTest {
  private NetworkingClient client;
  private MockWebServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    client = new NetworkingClient(new OkHttpClient(), mockServer.url("").toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    mockServer.shutdown();
  }

  @Test
  @DisplayName("should post to given url with correct request body")
  void testDoPost_whenSuccessfulResponse() throws IncogniaException {
    mockServer.setDispatcher(
        new Dispatcher() {
          @SneakyThrows
          @NotNull
          @Override
          public MockResponse dispatch(@NotNull RecordedRequest request) {
            if ("/v1/testurl".equals(request.getPath()) && "POST".equals(request.getMethod())) {
              String body =
                  IOUtils.toString(request.getBody().inputStream(), StandardCharsets.UTF_8);
              assertThat(body).isEqualTo("{\"id\":\"id\",\"long_id\":123}");
              return new MockResponse()
                  .setResponseCode(200)
                  .setBody("{\"name\": \"my awesome name\"}");
            }
            return new MockResponse().setResponseCode(404);
          }
        });
    TestResponseBody response =
        client.doPost("v1/testurl", new TestRequestBody("id", 123), TestResponseBody.class);
    assertThat(response.getName()).isEqualTo("my awesome name");
  }

  @Test
  @DisplayName("should get given url")
  void testDoGet_whenSuccessfulResponse() throws IncogniaException {
    mockServer.setDispatcher(
        new Dispatcher() {
          @SneakyThrows
          @NotNull
          @Override
          public MockResponse dispatch(@NotNull RecordedRequest request) {
            if ("/v1/testurl".equals(request.getPath()) && "GET".equals(request.getMethod())) {
              return new MockResponse()
                  .setResponseCode(200)
                  .setBody("{\"name\": \"my awesome name\"}");
            }
            return new MockResponse().setResponseCode(404);
          }
        });
    TestResponseBody response =
        client.doGet("v1/testurl", TestResponseBody.class, Collections.emptyMap());
    assertThat(response.getName()).isEqualTo("my awesome name");
  }

  @Test
  @DisplayName("should throw incognia api exception with correct fields")
  void testDoPost_whenInternalErrorResponse() {
    mockServer.setDispatcher(
        new Dispatcher() {
          @SneakyThrows
          @NotNull
          @Override
          public MockResponse dispatch(@NotNull RecordedRequest request) {
            if ("/v1/testurl".equals(request.getPath()) && "POST".equals(request.getMethod())) {
              String body =
                  IOUtils.toString(request.getBody().inputStream(), StandardCharsets.UTF_8);
              assertThat(body).isEqualTo("{\"id\":\"id\",\"long_id\":123}");
              return new MockResponse().setResponseCode(500);
            }
            return new MockResponse().setResponseCode(404);
          }
        });
    assertThatThrownBy(
            () ->
                client.doPost("v1/testurl", new TestRequestBody("id", 123), TestResponseBody.class))
        .satisfies(
            e -> {
              assertThat(e).isInstanceOf(IncogniaAPIException.class);
              assertThat(((IncogniaAPIException) e).getStatusCode()).isEqualTo(500);
            });
  }
}
