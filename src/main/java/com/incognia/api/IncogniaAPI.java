package com.incognia.api;

import com.incognia.api.clients.TokenAwareNetworkingClient;
import com.incognia.common.Address;
import com.incognia.common.exceptions.IncogniaAPIException;
import com.incognia.common.exceptions.IncogniaException;
import com.incognia.common.utils.Asserts;
import com.incognia.common.utils.CustomOptions;
import com.incognia.feedback.FeedbackEvent;
import com.incognia.feedback.FeedbackIdentifiers;
import com.incognia.feedback.PostFeedbackRequestBody;
import com.incognia.onboarding.PostSignupRequestBody;
import com.incognia.onboarding.RegisterSignupRequest;
import com.incognia.onboarding.RegisterWebSignupRequest;
import com.incognia.onboarding.SignupAssessment;
import com.incognia.transaction.AddressType;
import com.incognia.transaction.PostTransactionRequestBody;
import com.incognia.transaction.TransactionAddress;
import com.incognia.transaction.TransactionAssessment;
import com.incognia.transaction.login.RegisterLoginRequest;
import com.incognia.transaction.login.RegisterWebLoginRequest;
import com.incognia.transaction.payment.RegisterPaymentRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

/**
 * Class providing an implementation of the API endpoints described in <a
 * href="https://dash.incognia.com/api-reference">API reference</a>.
 *
 * <p>Automatically handles token generation and renewal.
 */
public class IncogniaAPI {
  private static final String API_URL = "https://api.incognia.com";
  private static final String EVALUATION_PARAMETER = "eval";
  private static final String DRY_RUN_PARAMETER = "dry_run";

  private final TokenAwareNetworkingClient tokenAwareNetworkingClient;

  private static final AtomicReference<IncogniaAPI> INSTANCE = new AtomicReference<>();

  /**
   * Creates a new instance for a given client id/secret.
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   */
  IncogniaAPI(String clientId, String clientSecret, CustomOptions options) {
    this(clientId, clientSecret, options, API_URL);
  }

  IncogniaAPI(String clientId, String clientSecret, CustomOptions options, String apiUrl) {
    Asserts.assertNotEmpty(clientId, "client id");
    Asserts.assertNotEmpty(clientSecret, "client secret");
    Asserts.assertNotEmpty(apiUrl, "api url");
    tokenAwareNetworkingClient =
        new TokenAwareNetworkingClient(
            new OkHttpClient.Builder()
                .callTimeout(options.getTimeoutMillis(), TimeUnit.MILLISECONDS)
                .connectionPool(
                    new ConnectionPool(
                        options.getMaxConnections(),
                        options.getKeepAliveSeconds(),
                        TimeUnit.SECONDS))
                .build(),
            apiUrl,
            clientId,
            clientSecret);
  }

  /**
   * Initializes a IncogniaAPI singleton instance and returns it
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   * @param options custom options that can be passed to the library
   * @return the singleton instance
   */
  public static IncogniaAPI init(String clientId, String clientSecret, CustomOptions options) {
    INSTANCE.compareAndSet(null, new IncogniaAPI(clientId, clientSecret, options));
    return INSTANCE.get();
  }

  /**
   * Initializes a IncogniaAPI singleton instance and returns it
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   * @return the singleton instance
   */
  public static IncogniaAPI init(String clientId, String clientSecret) {
    return init(clientId, clientSecret, CustomOptions.builder().build());
  }

  /**
   * Returns the singleton instance of IncogniaAPI if it was initialized using {@link #init(String,
   * String)}
   *
   * @return the singleton instance
   * @throws IllegalStateException if the instance was not initialized
   */
  public static IncogniaAPI instance() {
    if (INSTANCE.get() == null) {
      throw new IllegalStateException(
          "IncogniaAPI instance not initialized. Use IncogniaAPI.init(clientId, clientSecret) to set it.");
    }
    return INSTANCE.get();
  }

  /**
   * Registers a new signup for the given request token and address. Check <a
   * href="https://dash.incognia.com/api-reference#operation/signup-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
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
   *      RegisterSignupRequest signupRequest = RegisterSignupRequest.builder().requestToken(requestToken).address(address).build();
   *      SignupAssessment assessment = api.registerSignup(signupRequest);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterSignupRequest} model that contains the properties we need to
   *     make an assessment.
   * @return the assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public SignupAssessment registerSignup(RegisterSignupRequest request) throws IncogniaException {
    Asserts.assertNotNull(request, "register signup request");
    Optional<Address> address = Optional.ofNullable(request.getAddress());
    PostSignupRequestBody postSignupRequestBody =
        PostSignupRequestBody.builder()
            .installationId(request.getInstallationId())
            .requestToken(request.getRequestToken())
            .appVersion(request.getAppVersion())
            .deviceOs(
                Optional.ofNullable(request.getDeviceOs()).map(String::toLowerCase).orElse(null))
            .addressLine(address.map(Address::getAddressLine).orElse(null))
            .structuredAddress(address.map(Address::getStructuredAddress).orElse(null))
            .addressCoordinates(address.map(Address::getCoordinates).orElse(null))
            .externalId(request.getExternalId())
            .policyId(request.getPolicyId())
            .accountId(request.getAccountId())
            .additionalLocations(request.getAdditionalLocations())
            .customProperties(request.getCustomProperties())
            .build();
    return tokenAwareNetworkingClient.doPost(
        "api/v2/onboarding/signups", postSignupRequestBody, SignupAssessment.class);
  }

  /**
   * Registers a login to obtain a risk assessment. Check <a
   * href="https://dash.incognia.com/api-reference#operation/transactions-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
   * try {
   *     RegisterLoginRequest loginRequest = RegisterLoginRequest.builder()
   *         .requestToken("request-token")
   *         .accountId("account-id")
   *         .externalId("external-id")
   *         .policyId("policy-id")
   *         .evaluateTransaction(true) // can be omitted as it uses true as the default value
   *         .build();
   *      TransactionAssessment assessment = api.registerLogin(loginRequest);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterLoginRequest} model with the properties we need to make the
   *     assessment
   * @return the assessment for the login
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public TransactionAssessment registerLogin(RegisterLoginRequest request)
      throws IncogniaException {
    Asserts.assertNotNull(request, "register login request");
    Asserts.assertNotEmpty(request.getAccountId(), "account id");
    PostTransactionRequestBody requestBody =
        PostTransactionRequestBody.builder()
            .installationId(request.getInstallationId())
            .requestToken(request.getRequestToken())
            .appVersion(request.getAppVersion())
            .location(request.getLocation())
            .deviceOs(
                Optional.ofNullable(request.getDeviceOs()).map(String::toLowerCase).orElse(null))
            .accountId(request.getAccountId())
            .externalId(request.getExternalId())
            .policyId(request.getPolicyId())
            .customProperties(request.getCustomProperties())
            .type("login")
            .build();

    Map<String, String> queryParameters = new HashMap<>();
    if (request.shouldEvaluateTransaction() != null) {
      queryParameters.put(EVALUATION_PARAMETER, request.shouldEvaluateTransaction().toString());
    }
    return tokenAwareNetworkingClient.doPost(
        "api/v2/authentication/transactions",
        requestBody,
        TransactionAssessment.class,
        queryParameters);
  }

  /**
   * Registers a web login to obtain a risk assessment. Check <a
   * href="https://dash.incognia.com/api-reference#operation/transactions-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
   * try {
   *     RegisterLoginRequest loginRequest = RegisterLoginRequest.builder()
   *         .accountId("account-id")
   *         .externalId("external-id")
   *         .requestToken("request-token")
   *         .policyId("policy-id")
   *         .evaluateTransaction(true) // can be omitted as it uses true as the default value
   *         .build();
   *      TransactionAssessment assessment = api.registerLogin(loginRequest);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterWebLoginRequest} model with the properties we need to make
   *     the assessment
   * @return the assessment for the login
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public TransactionAssessment registerWebLogin(RegisterWebLoginRequest request)
      throws IncogniaException {
    Asserts.assertNotNull(request, "register login request");
    Asserts.assertNotEmpty(request.getAccountId(), "account id");
    Asserts.assertNotEmpty(
        Optional.ofNullable(request.getRequestToken()).orElseGet(request::getSessionToken),
        "request token");
    PostTransactionRequestBody requestBody =
        PostTransactionRequestBody.builder()
            .accountId(request.getAccountId())
            .externalId(request.getExternalId())
            .sessionToken(request.getSessionToken())
            .requestToken(request.getRequestToken())
            .policyId(request.getPolicyId())
            .type("login")
            .build();

    Map<String, String> queryParameters = new HashMap<>();
    if (request.shouldEvaluateTransaction() != null) {
      queryParameters.put(EVALUATION_PARAMETER, request.shouldEvaluateTransaction().toString());
    }
    return tokenAwareNetworkingClient.doPost(
        "api/v2/authentication/transactions",
        requestBody,
        TransactionAssessment.class,
        queryParameters);
  }

  /**
   * Registers a new signup for the given request token and address. Check <a
   * href="https://dash.incognia.com/api-reference#operation/signup-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
   * try {
   *      RegisterWebSignupRequest webSignupRequest = RegisterWebSignupRequest.builder().requestToken(requestToken).address(address).build();
   *      SignupAssessment assessment = api.registerSignup(webSignupRequest);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterWebSignupRequest} model that contains the properties we need
   *     to make an assessment.
   * @return the assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public SignupAssessment registerWebSignup(RegisterWebSignupRequest request)
      throws IncogniaException {
    Asserts.assertNotNull(request, "register signup request");
    Asserts.assertNotEmpty(
        Optional.ofNullable(request.getRequestToken()).orElseGet(request::getSessionToken),
        "request token");
    PostSignupRequestBody postSignupRequestBody =
        PostSignupRequestBody.builder()
            .sessionToken(request.getSessionToken())
            .requestToken(request.getRequestToken())
            .externalId(request.getExternalId())
            .policyId(request.getPolicyId())
            .accountId(request.getAccountId())
            .build();
    return tokenAwareNetworkingClient.doPost(
        "api/v2/onboarding/signups", postSignupRequestBody, SignupAssessment.class);
  }

  /**
   * Registers a payment to obtain a risk assessment. Check <a
   * href="https://dash.incognia.com/api-reference#operation/transactions-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
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
   *
   *      List<PaymentMethod> paymentMethods = new ArrayList<>();
   *        paymentMethods.add(
   *           PaymentMethod.builder()
   *               .creditCardInfo(
   *                   CardInfo.builder()
   *                       .bin("123456")
   *                       .expiryMonth("10")
   *                       .expiryYear("2028")
   *                       .lastFourDigits("4321")
   *                       .build())
   *               .type(PaymentType.CREDIT_CARD)
   *               .build());
   *
   *      RegisterPaymentRequest registerPaymentRequest =
   *          RegisterPaymentRequest.builder()
   *              .requestToken("request-token")
   *              .accountId("account-id")
   *              .externalId("external-id")
   *              .policyId("policy-id")
   *              .addresses(addresses)
   *              .evaluateTransaction(true) // can be omitted as it uses true as the default value
   *              .paymentValue(PaymentValue.builder().currency("BRL").amount(10.0).build())
   *              .paymentMethods(paymentMethods)
   *              .build();
   *
   *      TransactionAssessment assessment = api.registerPayment(registerPaymentRequest);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterPaymentRequest} with the fields we use to make an assessment
   * @return the payment's risk assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public TransactionAssessment registerPayment(RegisterPaymentRequest request)
      throws IncogniaException {
    Asserts.assertNotNull(request, "register payment request");
    Asserts.assertNotEmpty(request.getAccountId(), "account id");
    List<TransactionAddress> transactionAddresses =
        addressMapToTransactionAddresses(request.getAddresses());
    PostTransactionRequestBody requestBody =
        PostTransactionRequestBody.builder()
            .installationId(request.getInstallationId())
            .requestToken(request.getRequestToken())
            .appVersion(request.getAppVersion())
            .deviceOs(
                Optional.ofNullable(request.getDeviceOs()).map(String::toLowerCase).orElse(null))
            .accountId(request.getAccountId())
            .externalId(request.getExternalId())
            .policyId(request.getPolicyId())
            .type("payment")
            .addresses(transactionAddresses)
            .paymentValue(request.getPaymentValue())
            .paymentMethods(request.getPaymentMethods())
            .build();

    Map<String, String> queryParameters = new HashMap<>();
    if (request.shouldEvaluateTransaction() != null) {
      queryParameters.put(EVALUATION_PARAMETER, request.shouldEvaluateTransaction().toString());
    }

    return tokenAwareNetworkingClient.doPost(
        "api/v2/authentication/transactions",
        requestBody,
        TransactionAssessment.class,
        queryParameters);
  }

  /**
   * Shares feedback about a risk decision, improving the quality of risk assessments. Check <a
   * href="https://dash.incognia.com/api-reference#operation/feedbacks-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
   * try {
   *      Instant timestamp = Instant.now();
   *      client.registerFeedback(
   *         FeedbackEvent.ACCOUNT_TAKEOVER,
   *         timestamp,
   *         FeedbackIdentifiers.builder()
   *             .requestToken("request-token")
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
    registerFeedback(feedbackEvent, timestamp, identifiers, false);
  }

  public void registerFeedback(
      FeedbackEvent feedbackEvent,
      Instant timestamp,
      FeedbackIdentifiers identifiers,
      boolean dryRun)
      throws IncogniaException {
    PostFeedbackRequestBody requestBody =
        PostFeedbackRequestBody.builder()
            .event(feedbackEvent)
            .timestamp(timestamp.toEpochMilli())
            .installationId(identifiers.getInstallationId())
            .sessionToken(identifiers.getSessionToken())
            .accountId(identifiers.getAccountId())
            .loginId(identifiers.getLoginId())
            .paymentId(identifiers.getPaymentId())
            .signupId(identifiers.getSignupId())
            .externalId(identifiers.getExternalId())
            .requestToken(identifiers.getRequestToken())
            .expiresAt(
                Optional.ofNullable(identifiers.getExpiresAt()).map(Instant::toString).orElse(null))
            .build();

    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put(DRY_RUN_PARAMETER, String.valueOf(dryRun));
    tokenAwareNetworkingClient.doPost("api/v2/feedbacks", requestBody, queryParameters);
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
}
