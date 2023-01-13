package com.incognia.api.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incognia.feedback.PostFeedbackRequestBody;
import com.incognia.fixtures.ResourceUtils;
import com.incognia.onboarding.PostSignupRequestBody;
import com.incognia.transaction.PostTransactionRequestBody;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

public class TokenAwareDispatcher extends Dispatcher {
  private static final String USER_AGENT_HEADER =
      String.format(
          "incognia-api-java/%s (%s %s %s) Java/%s",
          com.incognia.api.ProjectVersion.PROJECT_VERSION,
          System.getProperty("os.name"),
          System.getProperty("os.version"),
          System.getProperty("os.arch"),
          System.getProperty("java.version"));
  private final String token;
  private final String clientId;
  private final String clientSecret;
  private final ObjectMapper objectMapper;
  @Setter private String expectedInstallationId;
  @Setter private String expectedExternalId;
  @Setter private String expectedAccountId;
  @Setter private String expectedPolicyId;
  @Setter private String expectedAddressLine;
  @Setter private UUID expectedSignupId;
  @Setter private PostTransactionRequestBody expectedTransactionRequestBody;
  @Setter private PostFeedbackRequestBody expectedFeedbackRequestBody;
  @Getter private int tokenRequestCount;

  public TokenAwareDispatcher(String token, String clientId, String clientSecret) {
    this.token = token;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.tokenRequestCount = 0;
    this.objectMapper = ObjectMapperFactory.OBJECT_MAPPER;
  }

  @SneakyThrows
  @NotNull
  @Override
  public MockResponse dispatch(@NotNull RecordedRequest request) {
    if ("/api/v2/onboarding".equals(request.getPath()) && "POST".equals(request.getMethod())) {
      assertThat(request.getHeader("Content-Type")).contains("application/json");
      assertThat(request.getHeader("User-Agent")).isEqualTo(USER_AGENT_HEADER);
      assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
      return new MockResponse().setResponseCode(200).setBody("{\"name\": \"my awesome name\"}");
    }
    if ("/api/v2/onboarding".equals(request.getPath()) && "GET".equals(request.getMethod())) {
      assertThat(request.getHeader("User-Agent")).isEqualTo(USER_AGENT_HEADER);
      assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
      return new MockResponse().setResponseCode(200).setBody("{\"name\": \"my awesome name\"}");
    }
    if ("/api/v2/onboarding/signups".equals(request.getPath())
        && "POST".equals(request.getMethod())) {
      return handlePostSignup(request);
    }
    if (("/api/v2/onboarding/signups/" + expectedSignupId).equals(request.getPath())
        && "GET".equals(request.getMethod())) {
      return handleGetSignup(request);
    }
    if (("/api/v2/authentication/transactions?eval=true".equals(request.getPath())
        || ("/api/v2/authentication/transactions".equals(request.getPath()))
            && "POST".equals(request.getMethod()))) {
      return handlePostTransaction(request);
    }
    if ("/api/v2/authentication/transactions?eval=false".equals(request.getPath())
        && "POST".equals(request.getMethod())) {
      return handlePostTransactionGivenFalseEval(request);
    }
    if ("/api/v2/feedbacks".equals(request.getPath()) && "POST".equals(request.getMethod())) {
      return handlePostFeedback(request);
    }
    if ("/api/v2/token".equals(request.getPath()) && "POST".equals(request.getMethod())) {
      return handleTokenRequest(request);
    }
    return new MockResponse().setResponseCode(404);
  }

  @SneakyThrows
  private MockResponse handlePostFeedback(RecordedRequest request) {
    assertThat(request.getHeader("Content-Type")).contains("application/json");
    assertThat(request.getHeader("User-Agent")).isEqualTo(USER_AGENT_HEADER);
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
    PostFeedbackRequestBody postFeedbackRequestBody =
        objectMapper.readValue(request.getBody().inputStream(), PostFeedbackRequestBody.class);
    assertThat(postFeedbackRequestBody).isEqualTo(expectedFeedbackRequestBody);
    return new MockResponse().setResponseCode(200);
  }

  @SneakyThrows
  private MockResponse handlePostTransaction(RecordedRequest request) {
    assertThat(request.getHeader("Content-Type")).contains("application/json");
    assertThat(request.getHeader("User-Agent")).isEqualTo(USER_AGENT_HEADER);
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
    PostTransactionRequestBody postTransactionRequestBody =
        objectMapper.readValue(request.getBody().inputStream(), PostTransactionRequestBody.class);
    assertThat(postTransactionRequestBody).isEqualTo(expectedTransactionRequestBody);
    String response = ResourceUtils.getResourceFileAsString("post_transaction_response.json");
    return new MockResponse().setResponseCode(200).setBody(response);
  }

  @SneakyThrows
  private MockResponse handlePostTransactionGivenFalseEval(RecordedRequest request) {
    assertThat(request.getHeader("Content-Type")).contains("application/json");
    assertThat(request.getHeader("User-Agent")).isEqualTo(USER_AGENT_HEADER);
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
    PostTransactionRequestBody postTransactionRequestBody =
        objectMapper.readValue(request.getBody().inputStream(), PostTransactionRequestBody.class);
    assertThat(postTransactionRequestBody).isEqualTo(expectedTransactionRequestBody);
    String response =
        ResourceUtils.getResourceFileAsString("post_transaction_given_false_eval_response.json");
    return new MockResponse().setResponseCode(200).setBody(response);
  }

  @NotNull
  private MockResponse handleGetSignup(@NotNull RecordedRequest request) {
    assertThat(request.getHeader("User-Agent")).isEqualTo(USER_AGENT_HEADER);
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
    String response = ResourceUtils.getResourceFileAsString("get_onboarding_response.json");
    return new MockResponse().setResponseCode(200).setBody(response);
  }

  @NotNull
  private MockResponse handlePostSignup(@NotNull RecordedRequest request)
      throws java.io.IOException {
    assertThat(request.getHeader("Content-Type")).contains("application/json");
    assertThat(request.getHeader("User-Agent")).isEqualTo(USER_AGENT_HEADER);
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + token);
    PostSignupRequestBody postSignupRequestBody =
        objectMapper.readValue(request.getBody().inputStream(), PostSignupRequestBody.class);
    assertThat(postSignupRequestBody.getInstallationId()).isEqualTo(expectedInstallationId);
    assertThat(postSignupRequestBody.getAccountId()).isEqualTo(expectedAccountId);
    assertThat(postSignupRequestBody.getExternalId()).isEqualTo(expectedExternalId);
    assertThat(postSignupRequestBody.getPolicyId()).isEqualTo(expectedPolicyId);
    assertThat(postSignupRequestBody.getAddressLine()).isEqualTo(expectedAddressLine);
    String response = ResourceUtils.getResourceFileAsString("post_onboarding_response.json");
    return new MockResponse().setResponseCode(200).setBody(response);
  }

  @SneakyThrows
  private MockResponse handleTokenRequest(@NotNull RecordedRequest request) {
    tokenRequestCount++;
    String authorizationHeader = request.getHeader("Authorization");
    assertThat(authorizationHeader).startsWith("Basic");
    assertThat(request.getHeader("Content-Type")).contains("application/x-www-form-urlencoded");
    String body = IOUtils.toString(request.getBody().inputStream(), StandardCharsets.UTF_8);
    assertThat(body).isEqualTo("grant_type=client_credentials");
    String[] idAndSecret =
        new String(
                Base64.getUrlDecoder().decode(authorizationHeader.split(" ")[1]),
                StandardCharsets.UTF_8)
            .split(":", 2);
    String clientId = idAndSecret[0];
    String clientSecret = idAndSecret[1];
    if (this.clientId.equals(clientId) && this.clientSecret.equals(clientSecret)) {
      return new MockResponse()
          .setResponseCode(200)
          .setBody(
              "{\"access_token\": \""
                  + token
                  + "\",\"expires_in\": 100,\"token_type\": \"Bearer\"}");
    }
    return new MockResponse().setResponseCode(401);
  }
}
