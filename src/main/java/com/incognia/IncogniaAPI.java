package com.incognia;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

public class IncogniaAPI {
  private static final String BR_API_URL = "https://incognia.inloco.com.br";
  private static final String US_API_URL = "https://api.us.incognia.com";
  private static final Map<Region, String> API_URLS = buildApiUrls();
  private static final Region DEFAULT_REGION = Region.US;

  private final TokenAwareNetworkingClient tokenAwareNetworkingClient;

  public IncogniaAPI(String clientId, String clientSecret) {
    this(clientId, clientSecret, DEFAULT_REGION);
  }

  public IncogniaAPI(String clientId, String clientSecret, Region region) {
    this(clientId, clientSecret, API_URLS.get(region != null ? region : DEFAULT_REGION));
  }

  IncogniaAPI(String clientId, String clientSecret, String apiUrl) {
    Asserts.assertNotEmpty(clientId, "client id");
    Asserts.assertNotEmpty(clientSecret, "client secret");
    Asserts.assertNotEmpty(apiUrl, "api url");
    // TODO (rato): set client timeout
    tokenAwareNetworkingClient =
        new TokenAwareNetworkingClient(
            new OkHttpClient.Builder().build(), apiUrl, clientId, clientSecret);
  }

  public SignupAssessment registerSignup(String installationId, Address address)
      throws IncogniaException {
    Asserts.assertNotEmpty(installationId, "installation id");
    Asserts.assertNotNull(address, "address");
    PostSignupRequestBody postSignupRequestBody =
        new PostSignupRequestBody(
            installationId,
            address.getAddressLine(),
            address.getStructuredAddress(),
            address.getCoordinates());
    return tokenAwareNetworkingClient.doPost(
        "api/v2/onboarding/signups", postSignupRequestBody, SignupAssessment.class);
  }

  public SignupAssessment getSignupAssessment(UUID signupId) throws IncogniaException {
    Asserts.assertNotNull(signupId, "signup id");
    String path = String.format("api/v2/onboarding/signups/%s", signupId);
    return tokenAwareNetworkingClient.doGet(path, SignupAssessment.class);
  }

  public void registerLogin(String installationId, String accountId) throws IncogniaException {
    registerLogin(installationId, accountId, null);
  }

  public TransactionAssessment registerLogin(
      String installationId, String accountId, String externalId) throws IncogniaException {
    Asserts.assertNotEmpty(installationId, "installation id");
    Asserts.assertNotEmpty(accountId, "account id");
    PostTransactionRequestBody requestBody =
        PostTransactionRequestBody.builder()
            .installationId(installationId)
            .accountId(accountId)
            .externalId(externalId)
            .type("login")
            .build();
    return tokenAwareNetworkingClient.doPost(
        "api/v2/authentication/transactions", requestBody, TransactionAssessment.class);
  }

  public TransactionAssessment registerPayment(String installationId, String accountId)
      throws IncogniaException {
    return registerPayment(installationId, accountId, null, Collections.emptyMap());
  }

  public TransactionAssessment registerPayment(
      String installationId, String accountId, String externalId) throws IncogniaException {
    return registerPayment(installationId, accountId, externalId, Collections.emptyMap());
  }

  public TransactionAssessment registerPayment(
      String installationId, String accountId, Map<AddressType, Address> addresses)
      throws IncogniaException {
    return registerPayment(installationId, accountId, null, addresses);
  }

  public TransactionAssessment registerPayment(
      String installationId,
      String accountId,
      String externalId,
      Map<AddressType, Address> addresses)
      throws IncogniaException {
    Asserts.assertNotEmpty(installationId, "installation id");
    Asserts.assertNotEmpty(accountId, "account id");
    List<TransactionAddress> transactionAddresses = addressMapToTransactionAddresses(addresses);
    PostTransactionRequestBody requestBody =
        PostTransactionRequestBody.builder()
            .installationId(installationId)
            .accountId(accountId)
            .externalId(externalId)
            .type("payment")
            .addresses(transactionAddresses)
            .build();
    return tokenAwareNetworkingClient.doPost(
        "api/v2/authentication/transactions", requestBody, TransactionAssessment.class);
  }

  public void registerFeedback(
      FeedbackEvent feedbackEvent, Instant timestamp, FeedbackIdentifiers identifiers)
      throws IncogniaException {
    PostFeedbackRequestBody requestBody =
        PostFeedbackRequestBody.builder()
            .event(feedbackEvent)
            .timestamp(timestamp.toEpochMilli())
            .installationId(identifiers.getInstallationId())
            .accountId(identifiers.getAccountId())
            .loginId(identifiers.getLoginId())
            .paymentId(identifiers.getPaymentId())
            .signupId(identifiers.getSignupId())
            .externalId(identifiers.getExternalId())
            .build();
    tokenAwareNetworkingClient.doPost("api/v2/feedbacks", requestBody);
  }

  @NotNull
  private List<TransactionAddress> addressMapToTransactionAddresses(
      Map<AddressType, Address> addresses) {
    return addresses.entrySet().stream()
        .map(
            entry -> {
              Address address = entry.getValue();
              return new TransactionAddress(
                  entry.getKey().name().toLowerCase(),
                  address.getAddressLine(),
                  address.getStructuredAddress(),
                  address.getCoordinates());
            })
        .collect(Collectors.toList());
  }
  // feedback

  private static Map<Region, String> buildApiUrls() {
    HashMap<Region, String> apiUrls = new HashMap<>();
    apiUrls.put(Region.BR, BR_API_URL);
    apiUrls.put(Region.US, US_API_URL);
    return Collections.unmodifiableMap(apiUrls);
  }
}
