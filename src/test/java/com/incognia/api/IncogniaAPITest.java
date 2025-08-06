package com.incognia.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import com.incognia.api.clients.TokenAwareDispatcher;
import com.incognia.common.Address;
import com.incognia.common.Coordinates;
import com.incognia.common.Location;
import com.incognia.common.PersonID;
import com.incognia.common.Reason;
import com.incognia.common.ReasonCode;
import com.incognia.common.ReasonSource;
import com.incognia.common.StructuredAddress;
import com.incognia.common.exceptions.IncogniaException;
import com.incognia.common.utils.ClientCredentials;
import com.incognia.common.utils.CustomOptions;
import com.incognia.feedback.FeedbackEvent;
import com.incognia.feedback.FeedbackIdentifiers;
import com.incognia.feedback.PostFeedbackRequestBody;
import com.incognia.fixtures.AddressFixture;
import com.incognia.onboarding.RegisterSignupRequest;
import com.incognia.onboarding.RegisterWebSignupRequest;
import com.incognia.onboarding.SignupAssessment;
import com.incognia.transaction.AddressType;
import com.incognia.transaction.PostTransactionRequestBody;
import com.incognia.transaction.TransactionAddress;
import com.incognia.transaction.TransactionAssessment;
import com.incognia.transaction.login.RegisterLoginRequest;
import com.incognia.transaction.login.RegisterWebLoginRequest;
import com.incognia.transaction.payment.CardInfo;
import com.incognia.transaction.payment.Coupon;
import com.incognia.transaction.payment.PaymentMethod;
import com.incognia.transaction.payment.PaymentType;
import com.incognia.transaction.payment.PaymentValue;
import com.incognia.transaction.payment.RegisterPaymentRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.SneakyThrows;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class IncogniaAPITest {
  private final String CLIENT_ID = "client-id";
  private final String CLIENT_SECRET = "client-secret";
  private final String DIFFERENT_CLIENT_ID = "different-client-id";
  private final String DIFFERENT_CLIENT_SECRET = "different-client-secret";
  private final TokenAwareDispatcher dispatcher =
      new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
  private MockWebServer mockServer;
  private IncogniaAPI client;
  private IncogniaAPI clientWithLowTimeout;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    client =
        new IncogniaAPI(
            CLIENT_ID,
            CLIENT_SECRET,
            CustomOptions.builder().build(),
            mockServer.url("").toString());
    clientWithLowTimeout =
        new IncogniaAPI(
            CLIENT_ID,
            CLIENT_SECRET,
            CustomOptions.builder().timeoutMillis(1L).build(),
            mockServer.url("").toString());
  }

  @AfterEach
  void tearDown() throws NoSuchFieldException, IOException, IllegalAccessException {
    resetIncogniaApiInstances();
    mockServer.shutdown();
  }

  @Test
  void testInit_shouldReturnAnInstance() {
    IncogniaAPI instance1 =
        IncogniaAPI.init(
            CLIENT_ID, CLIENT_SECRET, CustomOptions.builder().timeoutMillis(10000L).build());
    IncogniaAPI instance2 =
        IncogniaAPI.init(
            DIFFERENT_CLIENT_ID,
            DIFFERENT_CLIENT_SECRET,
            CustomOptions.builder().timeoutMillis(10000L).build());
    IncogniaAPI instance3 =
        IncogniaAPI.init(
            CLIENT_ID, CLIENT_SECRET, CustomOptions.builder().timeoutMillis(10000L).build());
    assertThat(instance1).isSameAs(instance3);
    assertThat(instance2).isNotSameAs(instance1);
  }

  @Test
  void testInstanceWithoutCredentials_shouldReturnAnInstance() {
    IncogniaAPI instance1 =
        IncogniaAPI.init(
            CLIENT_ID, CLIENT_SECRET, CustomOptions.builder().timeoutMillis(10000L).build());
    IncogniaAPI instance2 = IncogniaAPI.instance();
    assertThat(instance1).isSameAs(instance2);
  }

  @Test
  void
      testInstanceWithoutCredentials_whenTheNumberOfInstancesIsNotOne_shouldThrowIllegalStateException() {
    assertThrows(
        IllegalStateException.class, IncogniaAPI::instance); // No API instance has been created

    IncogniaAPI.init(
        CLIENT_ID, CLIENT_SECRET, CustomOptions.builder().timeoutMillis(10000L).build());
    IncogniaAPI.init(
        DIFFERENT_CLIENT_ID,
        DIFFERENT_CLIENT_SECRET,
        CustomOptions.builder().timeoutMillis(10000L).build());

    assertThrows(
        IllegalStateException.class,
        IncogniaAPI::instance); // Multiple IncogniaAPI instances have been created.
  }

  @Test
  void testInstanceWithCredentials_shouldReturnAnInstance() {
    IncogniaAPI instance1 =
        IncogniaAPI.init(
            CLIENT_ID, CLIENT_SECRET, CustomOptions.builder().timeoutMillis(10000L).build());
    IncogniaAPI instance2 =
        IncogniaAPI.init(
            DIFFERENT_CLIENT_ID,
            DIFFERENT_CLIENT_SECRET,
            CustomOptions.builder().timeoutMillis(10000L).build());
    IncogniaAPI instance3 = IncogniaAPI.instance(CLIENT_ID, CLIENT_SECRET);
    IncogniaAPI instance4 = IncogniaAPI.instance(DIFFERENT_CLIENT_ID, DIFFERENT_CLIENT_SECRET);

    assertThat(instance1).isSameAs(instance3);
    assertThat(instance2).isSameAs(instance4);
    assertThat(instance1).isNotSameAs(instance2);
  }

  @Test
  void testInstanceWithCredentials_whenNoInstanceWasCreated_shouldThrowIllegalStateException() {
    assertThrows(IllegalStateException.class, () -> IncogniaAPI.instance(CLIENT_ID, CLIENT_SECRET));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testInit_shouldCreateOkHttpWithRightParameters() {
    AtomicReference<List<Object>> poolArgs = new AtomicReference<>();

    try (MockedConstruction<OkHttpClient.Builder> builderConstr =
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
                (mock, context) -> {
                  poolArgs.set((List<Object>) context.arguments());
                })) {
      long timeoutMillis = generateRandomLong();
      long keepAliveSeconds = generateRandomLong();
      int maxConnections = generateRandomInteger();

      IncogniaAPI.init(
          CLIENT_ID,
          CLIENT_SECRET,
          CustomOptions.builder()
              .timeoutMillis(timeoutMillis)
              .keepAliveSeconds(keepAliveSeconds)
              .maxConnections(maxConnections)
              .build());

      verify(builderConstr.constructed().get(0)).callTimeout(timeoutMillis, TimeUnit.MILLISECONDS);

      assertThat(poolArgs.get())
          .containsExactly(maxConnections, keepAliveSeconds, TimeUnit.SECONDS);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testInit_whenNoCustomOptions_shouldCreateOkHttpWithDefaultParameters() {
    AtomicReference<List<Object>> poolArgs = new AtomicReference<>();

    try (MockedConstruction<OkHttpClient.Builder> builderConstr =
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
                (mock, context) -> {
                  poolArgs.set((List<Object>) context.arguments());
                })) {
      IncogniaAPI.init(CLIENT_ID, CLIENT_SECRET);

      verify(builderConstr.constructed().get(0)).callTimeout(10000L, TimeUnit.MILLISECONDS);

      assertThat(poolArgs.get()).containsExactly(5, 300L, TimeUnit.SECONDS);
    }
  }

  @Test
  @DisplayName("should return the expected signup response")
  @SneakyThrows
  void testRegisterSignup_whenDataIsValid() {
    String requestToken = "request-token";
    String accountId = "my-account";
    String policyId = UUID.randomUUID().toString();
    String externalId = "external-id";
    Address address = AddressFixture.ADDRESS_ADDRESS_LINE;
    Map<String, Object> map = new HashMap<>();
    map.put("custom-property", "custom-value");
    PersonID personId = PersonID.ofCPF("12345678901");

    dispatcher.setExpectedAddressLine(address.getAddressLine());
    dispatcher.setExpectedRequestToken(requestToken);
    dispatcher.setExpectedExternalId(externalId);
    dispatcher.setExpectedPolicyId(policyId);
    dispatcher.setExpectedAccountId(accountId);
    dispatcher.setExpectedCustomProperties(map);
    dispatcher.setExpectedPersonId(personId);
    mockServer.setDispatcher(dispatcher);
    RegisterSignupRequest registerSignupRequest =
        RegisterSignupRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .policyId(policyId)
            .externalId(externalId)
            .address(address)
            .customProperties(map)
            .personId(personId)
            .build();
    SignupAssessment signupAssessment = client.registerSignup(registerSignupRequest);
    assertThat(signupAssessment)
        .extracting("id", "requestId", "riskAssessment", "deviceId")
        .containsExactly(
            UUID.fromString("5e76a7ca-577c-4f47-a752-9e1e0cee9e49"),
            UUID.fromString("8afc84a7-f1d4-488d-bd69-36d9a37168b7"),
            Assessment.LOW_RISK,
            "1df6d999-556d-42c3-8c63-357e5d08d95b");
    Map<String, Object> locationServices = new HashMap<>();
    locationServices.put("location_permission_enabled", true);
    locationServices.put("location_sensors_enabled", true);
    Map<String, Object> deviceIntegrity = new HashMap<>();
    deviceIntegrity.put("probable_root", false);
    deviceIntegrity.put("emulator", false);
    deviceIntegrity.put("gps_spoofing", false);
    deviceIntegrity.put("from_official_store", true);

    Map<String, Object> expectedEvidence = new HashMap<>();
    expectedEvidence.put("device_model", "Moto Z2 Play");
    expectedEvidence.put("geocode_quality", "good");
    expectedEvidence.put("address_quality", "good");
    expectedEvidence.put("address_match", "street");
    expectedEvidence.put("location_events_near_address", 38);
    expectedEvidence.put("location_events_quantity", 288);
    expectedEvidence.put("location_services", locationServices);
    expectedEvidence.put("device_integrity", deviceIntegrity);

    assertThat(signupAssessment.getEvidence()).containsExactlyInAnyOrderEntriesOf(expectedEvidence);

    Reason expectedReason =
        Reason.builder()
            .code(ReasonCode.TRUSTED_LOCATION.getCode())
            .source(ReasonSource.LOCAL.getSource())
            .build();
    assertThat(signupAssessment.getReasons()).containsExactly(expectedReason);
  }

  @Test
  @DisplayName("should return the expected signup response when the address is empty")
  @SneakyThrows
  void testRegisterSignup_withEmptyAddress() {
    String requestToken = "request-token";
    String accountId = "my-account";
    String policyId = UUID.randomUUID().toString();
    String externalId = "external-id";
    String appVersion = "1.4.3";
    String deviceOs = "iOS";

    dispatcher.setExpectedAddressLine(null);
    dispatcher.setExpectedRequestToken(requestToken);
    dispatcher.setExpectedExternalId(externalId);
    dispatcher.setExpectedPolicyId(policyId);
    dispatcher.setExpectedAccountId(accountId);
    dispatcher.setExpectedAppVersion(appVersion);
    dispatcher.setExpectedDeviceOs(deviceOs.toLowerCase());
    dispatcher.setExpectedCustomProperties(null);
    mockServer.setDispatcher(dispatcher);
    RegisterSignupRequest registerSignupRequest =
        RegisterSignupRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .policyId(policyId)
            .externalId(externalId)
            .appVersion(appVersion)
            .deviceOs(deviceOs)
            .customProperties(null)
            .build();
    SignupAssessment signupAssessment = client.registerSignup(registerSignupRequest);
    assertThat(signupAssessment)
        .extracting("id", "requestId", "riskAssessment", "deviceId")
        .containsExactly(
            UUID.fromString("5e76a7ca-577c-4f47-a752-9e1e0cee9e49"),
            UUID.fromString("8afc84a7-f1d4-488d-bd69-36d9a37168b7"),
            Assessment.HIGH_RISK,
            "1df6d999-556d-42c3-8c63-357e5d08d95b");
    Map<String, Object> locationServices = new HashMap<>();
    locationServices.put("location_permission_enabled", true);
    locationServices.put("location_sensors_enabled", true);
    Map<String, Object> deviceIntegrity = new HashMap<>();
    deviceIntegrity.put("probable_root", true);
    deviceIntegrity.put("emulator", false);
    deviceIntegrity.put("gps_spoofing", false);
    deviceIntegrity.put("from_official_store", true);

    Map<String, Object> expectedEvidence = new HashMap<>();
    expectedEvidence.put("device_model", "Moto Z2 Play");
    expectedEvidence.put("location_services", locationServices);
    expectedEvidence.put("device_integrity", deviceIntegrity);

    assertThat(signupAssessment.getEvidence()).containsExactlyInAnyOrderEntriesOf(expectedEvidence);

    Reason expectedReason =
        Reason.builder()
            .code(ReasonCode.DEVICE_INTEGRITY.getCode())
            .source(ReasonSource.LOCAL.getSource())
            .build();
    assertThat(signupAssessment.getReasons()).containsExactly(expectedReason);
  }

  @Test
  @DisplayName("should return the expected web signup response")
  @SneakyThrows
  void testRegisterWebSignup_whenDataIsValid() {
    String requestToken = "request-token-web-signup";
    String accountId = "my-account";
    String policyId = UUID.randomUUID().toString();
    String externalId = "external-id";
    Map<String, Object> map = new HashMap<>();
    map.put("custom-property", "custom-value");
    PersonID personId = PersonID.ofCPF("12345678901");

    dispatcher.setExpectedRequestToken(requestToken);
    dispatcher.setExpectedExternalId(externalId);
    dispatcher.setExpectedPolicyId(policyId);
    dispatcher.setExpectedAccountId(accountId);
    dispatcher.setExpectedCustomProperties(map);
    dispatcher.setExpectedPersonId(personId);
    mockServer.setDispatcher(dispatcher);
    RegisterWebSignupRequest registerSignupRequest =
        RegisterWebSignupRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .policyId(policyId)
            .externalId(externalId)
            .customProperties(map)
            .personId(personId)
            .build();
    SignupAssessment webSignupAssessment = client.registerWebSignup(registerSignupRequest);
    assertThat(webSignupAssessment)
        .extracting("id", "requestId", "riskAssessment", "deviceId")
        .containsExactly(
            UUID.fromString("5e76a7ca-577c-4f47-a752-9e1e0cee9e49"),
            UUID.fromString("8afc84a7-f1d4-488d-bd69-36d9a37168b7"),
            Assessment.LOW_RISK,
            "1df6d999-556d-42c3-8c63-357e5d08d95b");
    Map<String, Object> locationServices = new HashMap<>();
    locationServices.put("location_permission_enabled", true);
    locationServices.put("location_sensors_enabled", true);
    Map<String, Object> deviceIntegrity = new HashMap<>();
    deviceIntegrity.put("probable_root", false);
    deviceIntegrity.put("emulator", false);
    deviceIntegrity.put("gps_spoofing", false);
    deviceIntegrity.put("from_official_store", true);

    Map<String, Object> expectedEvidence = new HashMap<>();
    expectedEvidence.put("device_model", "Moto Z2 Play");
    expectedEvidence.put("geocode_quality", "good");
    expectedEvidence.put("address_quality", "good");
    expectedEvidence.put("address_match", "street");
    expectedEvidence.put("location_events_near_address", 38);
    expectedEvidence.put("location_events_quantity", 288);
    expectedEvidence.put("location_services", locationServices);
    expectedEvidence.put("device_integrity", deviceIntegrity);

    assertThat(webSignupAssessment.getEvidence())
        .containsExactlyInAnyOrderEntriesOf(expectedEvidence);

    Reason expectedReason =
        Reason.builder()
            .code(ReasonCode.TRUSTED_LOCATION.getCode())
            .source(ReasonSource.LOCAL.getSource())
            .build();
    assertThat(webSignupAssessment.getReasons()).containsExactly(expectedReason);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true})
  @NullSource
  @DisplayName("should return IncogniaException if exceeds timeout")
  @SneakyThrows
  void testRegisterLogin_whenReachesTheTimeout(Boolean eval) {
    String requestToken = "request-token";
    String accountId = "account-id";
    String policyId = "policy-id";

    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .type("login")
            .addresses(null)
            .paymentMethods(null)
            .policyId(policyId)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterLoginRequest loginRequest =
        RegisterLoginRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .evaluateTransaction(eval)
            .policyId(policyId)
            .build();
    assertThatThrownBy(() -> clientWithLowTimeout.registerLogin(loginRequest))
        .isInstanceOf(IncogniaException.class)
        .hasMessage("network call timeout");
  }

  @ParameterizedTest
  @ValueSource(booleans = {true})
  @NullSource
  @DisplayName("should return the expected login transaction response")
  @SneakyThrows
  void testRegisterLogin_whenDataIsValid(Boolean eval) {
    String requestToken = "request-token";
    String accountId = "account-id";
    String appVersion = "1.4.3";
    Location location =
        Location.builder()
            .latitude("40.74836007062138")
            .longitude("-73.98509720487937")
            .collectedAt(Instant.now().toString())
            .build();
    String deviceOs = "Android";
    String externalId = "external-id";
    String policyId = "policy-id";
    String relatedAccountId = "related-account-id";
    Map<String, Object> map = new HashMap<>();
    map.put("custom-property", "custom-value");
    PersonID personId = PersonID.ofCPF("12345678901");

    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .requestToken(requestToken)
            .externalId(externalId)
            .appVersion(appVersion)
            .location(location)
            .deviceOs(deviceOs.toLowerCase())
            .accountId(accountId)
            .type("login")
            .addresses(null)
            .paymentMethods(null)
            .policyId(policyId)
            .relatedAccountId(relatedAccountId)
            .customProperties(map)
            .personId(personId)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterLoginRequest loginRequest =
        RegisterLoginRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .appVersion(appVersion)
            .location(location)
            .deviceOs(deviceOs)
            .externalId(externalId)
            .evaluateTransaction(eval)
            .policyId(policyId)
            .relatedAccountId(relatedAccountId)
            .customProperties(map)
            .personId(personId)
            .build();
    TransactionAssessment transactionAssessment = client.registerLogin(loginRequest);
    assertTransactionAssessment(transactionAssessment);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true})
  @NullSource
  @DisplayName("should return the expected web login transaction response")
  @SneakyThrows
  void testRegisterWebLogin_whenDataIsValid(Boolean eval) {
    String accountId = "account-id";
    String externalId = "external-id";
    String requestToken = "request-token";
    String policyId = "policy-id";
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put("string-property", "string-value");
    customProperties.put("float-property", 12.345);
    PersonID personId = PersonID.ofCPF("12345678901");

    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .externalId(externalId)
            .accountId(accountId)
            .type("login")
            .requestToken(requestToken)
            .addresses(null)
            .paymentMethods(null)
            .policyId(policyId)
            .customProperties(customProperties)
            .personId(personId)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterWebLoginRequest loginRequest =
        RegisterWebLoginRequest.builder()
            .accountId(accountId)
            .externalId(externalId)
            .evaluateTransaction(eval)
            .requestToken(requestToken)
            .policyId(policyId)
            .customProperties(customProperties)
            .personId(personId)
            .build();
    TransactionAssessment transactionAssessment = client.registerWebLogin(loginRequest);
    assertTransactionAssessment(transactionAssessment);
  }

  @Test
  @DisplayName("should return an empty response")
  @SneakyThrows
  void testRegisterLogin_whenEvalIsFalse() {
    String requestToken = "request-token";
    String accountId = "account-id";
    String externalId = "external-id";
    String policyId = "policy-id";

    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .requestToken(requestToken)
            .externalId(externalId)
            .accountId(accountId)
            .type("login")
            .addresses(null)
            .paymentMethods(null)
            .policyId(policyId)
            .customProperties(null)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterLoginRequest loginRequest =
        RegisterLoginRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .externalId(externalId)
            .evaluateTransaction(false)
            .policyId(policyId)
            .build();
    TransactionAssessment transactionAssessment = client.registerLogin(loginRequest);
    assertThat(transactionAssessment).isEqualTo(TransactionAssessment.builder().build());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true})
  @NullSource
  @DisplayName("should return the expected payment transaction response")
  @SneakyThrows
  void testRegisterPayment_whenDataIsValid(Boolean eval) {
    String requestToken = "request-token";
    String accountId = "account-id";
    String appVersion = "appVersion";
    String deviceOs = "iOS";
    String externalId = "external-id";
    String policyId = "policy-id";
    String storeId = "store-id";
    Location location =
        Location.builder()
            .latitude("40.74836007062138")
            .longitude("-73.98509720487937")
            .collectedAt(Instant.now().toString())
            .build();
    Coupon coupon =
        Coupon.builder()
            .type("percent_off")
            .value(10.0)
            .maxDiscount(5.0)
            .id("coupon-id")
            .name("coupon-name")
            .build();
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put("custom-property", "custom-value");
    customProperties.put("float-property", 12.345);
    Address address =
        Address.builder()
            .structuredAddress(
                StructuredAddress.builder()
                    .countryCode("US")
                    .countryName("United States of America")
                    .locale("en-US")
                    .state("NY")
                    .city("New York City")
                    .borough("Manhattan")
                    .neighborhood("Midtown")
                    .street("W 34th St.")
                    .number("20")
                    .complements("Floor 2")
                    .postalCode("10001")
                    .build())
            .coordinates(new Coordinates(40.74836007062138, -73.98509720487937))
            .build();
    List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
    paymentMethods.add(
        PaymentMethod.builder()
            .creditCardInfo(
                CardInfo.builder()
                    .bin("1234")
                    .expiryMonth("10")
                    .expiryYear("28")
                    .lastFourDigits("4321")
                    .build())
            .type(PaymentType.CREDIT_CARD)
            .build());
    PaymentValue paymentValue = PaymentValue.builder().amount(13.0).currency("BRL").build();
    PersonID personId = PersonID.ofCPF("12345678901");

    List<TransactionAddress> transactionAddresses =
        Collections.singletonList(
            new TransactionAddress(
                "shipping", null, address.getStructuredAddress(), address.getCoordinates()));
    RegisterPaymentRequest paymentRequest =
        RegisterPaymentRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .appVersion(appVersion)
            .deviceOs(deviceOs)
            .externalId(externalId)
            .policyId(policyId)
            .addresses(Collections.singletonMap(AddressType.SHIPPING, address))
            .evaluateTransaction(eval)
            .paymentValue(paymentValue)
            .paymentMethods(paymentMethods)
            .storeId(storeId)
            .coupon(coupon)
            .customProperties(customProperties)
            .location(location)
            .personId(personId)
            .build();
    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .requestToken(requestToken)
            .externalId(externalId)
            .policyId(policyId)
            .accountId(accountId)
            .appVersion(appVersion)
            .deviceOs(deviceOs.toLowerCase())
            .type("payment")
            .addresses(transactionAddresses)
            .paymentValue(paymentValue)
            .paymentMethods(paymentMethods)
            .storeId(storeId)
            .coupon(coupon)
            .customProperties(customProperties)
            .location(location)
            .personId(personId)
            .build());
    mockServer.setDispatcher(dispatcher);
    TransactionAssessment transactionAssessment = client.registerPayment(paymentRequest);
    assertTransactionAssessment(transactionAssessment);
  }

  @Test
  @DisplayName("should return an empty response")
  @SneakyThrows
  void testRegisterPayment_whenEvalIsFalse() {
    String requestToken = "request-token";
    String accountId = "account-id";
    String externalId = "external-id";
    String policyId = "policy-id";
    Address address =
        Address.builder()
            .structuredAddress(
                StructuredAddress.builder()
                    .countryCode("US")
                    .countryName("United States of America")
                    .locale("en-US")
                    .state("NY")
                    .city("New York City")
                    .borough("Manhattan")
                    .neighborhood("Midtown")
                    .street("W 34th St.")
                    .number("20")
                    .complements("Floor 2")
                    .postalCode("10001")
                    .build())
            .coordinates(new Coordinates(40.74836007062138, -73.98509720487937))
            .build();

    List<TransactionAddress> transactionAddresses =
        Collections.singletonList(
            new TransactionAddress(
                "shipping", null, address.getStructuredAddress(), address.getCoordinates()));
    List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
    paymentMethods.add(
        PaymentMethod.builder()
            .creditCardInfo(
                CardInfo.builder()
                    .bin("1234")
                    .expiryMonth("10")
                    .expiryYear("28")
                    .lastFourDigits("4321")
                    .build())
            .type(PaymentType.CREDIT_CARD)
            .build());
    PaymentValue paymentValue = PaymentValue.builder().amount(13.0).currency("BRL").build();
    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .requestToken(requestToken)
            .externalId(externalId)
            .policyId(policyId)
            .accountId(accountId)
            .type("payment")
            .addresses(transactionAddresses)
            .paymentMethods(paymentMethods)
            .paymentValue(paymentValue)
            .customProperties(null)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterPaymentRequest paymentRequest =
        RegisterPaymentRequest.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .externalId(externalId)
            .policyId(policyId)
            .addresses(Collections.singletonMap(AddressType.SHIPPING, address))
            .evaluateTransaction(false)
            .paymentValue(paymentValue)
            .paymentMethods(paymentMethods)
            .build();
    TransactionAssessment transactionAssessment = client.registerPayment(paymentRequest);
    assertThat(transactionAssessment).isEqualTo(TransactionAssessment.builder().build());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("should be successful")
  @SneakyThrows
  void testRegisterFeedback_whenDataIsValid(boolean dryRun) {
    String requestToken = "request-token";
    String accountId = "account-id";
    String externalId = "external-id";
    String signupId = UUID.randomUUID().toString();
    Instant timestamp = Instant.now();
    PersonID personId = PersonID.ofCPF("12345678901");

    dispatcher.setExpectedFeedbackRequestBody(
        PostFeedbackRequestBody.builder()
            .requestToken(requestToken)
            .externalId(externalId)
            .signupId(signupId)
            .accountId(accountId)
            .event(FeedbackEvent.ACCOUNT_TAKEOVER)
            .timestamp(timestamp.toEpochMilli())
            .personId(personId)
            .build());
    mockServer.setDispatcher(dispatcher);
    client.registerFeedback(
        FeedbackEvent.ACCOUNT_TAKEOVER,
        timestamp,
        FeedbackIdentifiers.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .externalId(externalId)
            .signupId(signupId)
            .personId(personId)
            .build(),
        dryRun);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("should be successful with expiresAt")
  @SneakyThrows
  void testRegisterFeedback_withExpiresAt(boolean dryRun) {
    String requestToken = "request-token";
    String accountId = "account-id";
    String externalId = "external-id";
    String signupId = UUID.randomUUID().toString();
    Instant timestamp = Instant.now();
    Instant expiresAt = timestamp.plusSeconds(3600);

    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(CLIENT_ID, CLIENT_SECRET);
    dispatcher.setExpectedFeedbackRequestBody(
        PostFeedbackRequestBody.builder()
            .requestToken(requestToken)
            .externalId(externalId)
            .signupId(signupId)
            .accountId(accountId)
            .event(FeedbackEvent.ACCOUNT_TAKEOVER)
            .timestamp(timestamp.toEpochMilli())
            .expiresAt(expiresAt.toString())
            .build());
    mockServer.setDispatcher(dispatcher);
    client.registerFeedback(
        FeedbackEvent.ACCOUNT_TAKEOVER,
        timestamp,
        FeedbackIdentifiers.builder()
            .requestToken(requestToken)
            .accountId(accountId)
            .externalId(externalId)
            .signupId(signupId)
            .expiresAt(expiresAt)
            .build(),
        dryRun);
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterPayment_whenAccountIdIsNotValid() {
    assertThatThrownBy(
            () ->
                client.registerPayment(
                    RegisterPaymentRequest.builder()
                        .requestToken("request-token")
                        .accountId("")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
    assertThatThrownBy(
            () ->
                client.registerPayment(
                    RegisterPaymentRequest.builder()
                        .requestToken("request-token")
                        .accountId(null)
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterLogin_whenAccountIdIsNotValid() {
    assertThatThrownBy(
            () ->
                client.registerLogin(
                    RegisterLoginRequest.builder()
                        .requestToken("request token")
                        .accountId("")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
    assertThatThrownBy(
            () ->
                client.registerLogin(
                    RegisterLoginRequest.builder()
                        .requestToken("request token")
                        .accountId(null)
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
  }

  private void assertTransactionAssessment(TransactionAssessment transactionAssessment) {
    assertThat(transactionAssessment)
        .extracting("id", "riskAssessment", "deviceId")
        .containsExactly(
            UUID.fromString("dfe1f2ff-8f0d-4ce8-aed1-af8435143044"),
            Assessment.LOW_RISK,
            "1df6d999-556d-42c3-8c63-357e5d08d95b");
    Map<String, Object> locationServices = new HashMap<>();
    locationServices.put("location_permission_enabled", true);
    locationServices.put("location_sensors_enabled", true);
    Map<String, Object> deviceIntegrity = new HashMap<>();
    deviceIntegrity.put("probable_root", false);
    deviceIntegrity.put("emulator", false);
    deviceIntegrity.put("gps_spoofing", false);
    deviceIntegrity.put("from_official_store", true);

    Map<String, Object> expectedEvidence = new HashMap<>();
    expectedEvidence.put("device_model", "Moto Z2 Play");
    expectedEvidence.put("device_fraud_reputation", "unknown");
    expectedEvidence.put("distance_to_trusted_location", 21.06295635345013);
    expectedEvidence.put("last_location_ts", "2022-11-01T22:45:53.299Z");
    expectedEvidence.put("sensor_match_type", "gps");
    expectedEvidence.put("location_events_quantity", 62);
    expectedEvidence.put("location_services", locationServices);
    expectedEvidence.put("device_integrity", deviceIntegrity);

    assertThat(transactionAssessment.getEvidence()).containsAllEntriesOf(expectedEvidence);

    Reason expectedReason =
        Reason.builder()
            .code(ReasonCode.TRUSTED_LOCATION.getCode())
            .source(ReasonSource.LOCAL.getSource())
            .build();
    assertThat(transactionAssessment.getReasons()).containsExactly(expectedReason);
  }

  private static long generateRandomLong() {
    return new Random().nextLong() & Long.MAX_VALUE;
  }

  private static int generateRandomInteger() {
    return new Random().nextInt() & Integer.MAX_VALUE;
  }

  private static void resetIncogniaApiInstances()
      throws NoSuchFieldException, IllegalAccessException {
    Field field = IncogniaAPI.class.getDeclaredField("INSTANCES");
    field.setAccessible(true);

    @SuppressWarnings("unchecked")
    ConcurrentHashMap<ClientCredentials, IncogniaAPI> map =
        (ConcurrentHashMap<ClientCredentials, IncogniaAPI>) field.get(null);
    map.clear();
  }
}
