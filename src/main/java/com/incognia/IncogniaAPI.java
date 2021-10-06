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

/**
 * Class providing an implementation of the API endpoints described in
 * https://dash.incognia.com/api-reference.
 *
 * <p>Automatically handles token generation and renewal.
 */
public class IncogniaAPI {
  private static final String BR_API_URL = "https://api.br.incognia.com";
  private static final String US_API_URL = "https://api.us.incognia.com";
  private static final Map<Region, String> API_URLS = buildApiUrls();
  private static final Region DEFAULT_REGION = Region.US;

  private final TokenAwareNetworkingClient tokenAwareNetworkingClient;

  /**
   * Creates a new instance for a given client id/secret. Uses the default {@link Region}
   * (Region.US)
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   * @see #IncogniaAPI(String, String, Region)
   */
  public IncogniaAPI(String clientId, String clientSecret) {
    this(clientId, clientSecret, DEFAULT_REGION);
  }

  /**
   * Creates a new instance for a given client id/secret and a {@link Region}
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   * @param region the region to be used
   */
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

  /**
   * Registers a new signup for the given installation and address. Check <a
   * href="https://dash.incognia.com/api-reference#operation/signup-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
   * try {
   *      Address address = Address address =
   *         Address.builder()
   *             .structuredAddress(
   *                 StructuredAddress.builder()
   *                     .countryCode("US")
   *                     .countryName("United States of America")
   *                     .locale("en-US")
   *                     .state("NY")
   *                     .city("New York City")
   *                     .borough("Manhattan")
   *                     .neighborhood("Midtown")
   *                     .street("W 34th St.")
   *                     .number("20")
   *                     .complements("Floor 2")
   *                     .postalCode("10001")
   *                     .build())
   *             .coordinates(new Coordinates(40.74836007062138, -73.98509720487937))
   *             .build();
   *      SignupAssessment assessment = api.registerSignup("installation id", address);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param installationId the <a
   *     href="https://docs.incognia.com/learning/concepts/identifiers/installation-id">installation
   *     id</a>
   * @param address the address
   * @return the assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
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

  /**
   * Gets the latest assessment for a given signup. Check <a
   * href="https://dash.incognia.com/api-reference#operation/signup-get">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
   * try {
   *      UUID signupId = UUID.fromString("c9ac2803-c868-4b7a-8323-8a6b96298ebe");
   *      SignupAssessment assessment = api.getSignupAssessment(signupId);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param signupId the signup id
   * @return the latest assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public SignupAssessment getSignupAssessment(UUID signupId) throws IncogniaException {
    Asserts.assertNotNull(signupId, "signup id");
    String path = String.format("api/v2/onboarding/signups/%s", signupId);
    return tokenAwareNetworkingClient.doGet(path, SignupAssessment.class);
  }

  /**
   * Registers a login without external id.
   *
   * @see #registerLogin(String, String, String)
   * @param installationId the <a
   *     href="https://docs.incognia.com/learning/concepts/identifiers/installation-id">installation
   *     id</a>
   * @param accountId the account id
   * @return the assessment for the login
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public TransactionAssessment registerLogin(String installationId, String accountId)
      throws IncogniaException {
    return registerLogin(installationId, accountId, null);
  }

  /**
   * Registers a login to obtain a risk assessment. Check <a
   * href="https://dash.incognia.com/api-reference#operation/transactions-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
   * try {
   *      TransactionAssessment assessment = api.registerLogin("installation-id", "account-id", "external-id");
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param installationId the <a
   *     href="https://docs.incognia.com/learning/concepts/identifiers/installation-id">installation
   *     id</a>
   * @param accountId the account id
   * @param externalId client-owned transaction identifier
   * @return the assessment for the login
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
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

  /**
   * Registers a payment without external id and addresses. Equivalent to {@link
   * #registerPayment(String, String, String, Map)} with null external id and empty addresses.
   *
   * @see #registerPayment(String, String, String, Map)
   */
  public TransactionAssessment registerPayment(String installationId, String accountId)
      throws IncogniaException {
    return registerPayment(installationId, accountId, null, Collections.emptyMap());
  }

  /**
   * Registers a payment without addresses. Equivalent to {@link #registerPayment(String, String,
   * String, Map)} with empty addresses.
   *
   * @see #registerPayment(String, String, String, Map)
   */
  public TransactionAssessment registerPayment(
      String installationId, String accountId, String externalId) throws IncogniaException {
    return registerPayment(installationId, accountId, externalId, Collections.emptyMap());
  }
  /**
   * Registers a payment without external id. Equivalent to {@link #registerPayment(String, String,
   * String, Map)} with null external id.
   *
   * @see #registerPayment(String, String, String, Map)
   */
  public TransactionAssessment registerPayment(
      String installationId, String accountId, Map<AddressType, Address> addresses)
      throws IncogniaException {
    return registerPayment(installationId, accountId, null, addresses);
  }

  /**
   * Registers a payment to obtain a risk assessment. Check <a
   * href="https://dash.incognia.com/api-reference#operation/transactions-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
   * try {
   *      Address address = Address address =
   *         Address.builder()
   *             .structuredAddress(
   *                 StructuredAddress.builder()
   *                     .countryCode("US")
   *                     .countryName("United States of America")
   *                     .locale("en-US")
   *                     .state("NY")
   *                     .city("New York City")
   *                     .borough("Manhattan")
   *                     .neighborhood("Midtown")
   *                     .street("W 34th St.")
   *                     .number("20")
   *                     .complements("Floor 2")
   *                     .postalCode("10001")
   *                     .build())
   *             .coordinates(new Coordinates(40.74836007062138, -73.98509720487937))
   *             .build();
   *      Map<AddressType, Address> addresses = Map.of(
   *          AddressType.SHIPPING, address
   *          AddressType.BILLING, address);
   *      TransactionAssessment assessment = api.registerPayment("installation-id", "account-id", "external-id");
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param installationId the <a
   *     href="https://docs.incognia.com/learning/concepts/identifiers/installation-id">installation
   *     id</a>
   * @param accountId the account id
   * @param externalId client-owned transaction identifier
   * @param addresses addresses related to this payment
   * @return the payment's risk assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
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

  /**
   * Shares feedback about a risk decision, improving the quality of risk assessments. Check <a
   * href="https://dash.incognia.com/api-reference#operation/feedbacks-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
   * try {
   *      Instant timestamp = Instant.now();
   *      client.registerFeedback(
   *         FeedbackEvent.ACCOUNT_TAKEOVER,
   *         timestamp,
   *         FeedbackIdentifiers.builder()
   *             .installationId("installation-id")
   *             .accountId("account-id")
   *             .externalId("external-id")
   *             .signupId("c9ac2803-c868-4b7a-8323-8a6b96298ebe")
   *             .build();
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param feedbackEvent type of feedback event
   * @param timestamp Instant when the fraud or event happened
   * @param identifiers the user's identifiers
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
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
