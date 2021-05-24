package com.incognia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.incognia.fixtures.TestRequestBody;
import com.incognia.fixtures.TestResponseBody;
import com.incognia.fixtures.TokenAwareDispatcher;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenAwareNetworkingClientTest {
  private static final String CLIENT_ID = "client-id";
  private static final String CLIENT_SECRET = "client-secret";
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
  void testDoPost_whenNoTokenExistsYetAndCredentialsAreValid() throws IncogniaException {
    String token = createToken();
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);
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
  @DisplayName("should get a 401 error")
  void testDoPost_whenNoTokenExistsYetAndCredentialsAreInvalid() {
    String token = createToken();
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, "invalid", CLIENT_SECRET);
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

  @SneakyThrows
  private String createToken() {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(1024);
    KeyPair keyPair = keyGen.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
    return JWT.create()
        .withIssuer("incognia")
        .withExpiresAt(Date.from(Instant.now().plusSeconds(100)))
        .sign(algorithm);
  }
}
