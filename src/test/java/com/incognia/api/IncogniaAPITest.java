package com.incognia.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.incognia.api.clients.TokenAwareDispatcher;
import com.incognia.common.Address;
import com.incognia.common.Coordinates;
import com.incognia.common.Reason;
import com.incognia.common.StructuredAddress;
import com.incognia.feedback.FeedbackEvent;
import com.incognia.feedback.FeedbackIdentifiers;
import com.incognia.feedback.PostFeedbackRequestBody;
import com.incognia.fixtures.AddressFixture;
import com.incognia.fixtures.TokenCreationFixture;
import com.incognia.onboarding.RegisterSignupRequest;
import com.incognia.onboarding.SignupAssessment;
import com.incognia.transaction.AddressType;
import com.incognia.transaction.PostTransactionRequestBody;
import com.incognia.transaction.TransactionAddress;
import com.incognia.transaction.TransactionAssessment;
import com.incognia.transaction.login.RegisterLoginRequest;
import com.incognia.transaction.payment.CardInfo;
import com.incognia.transaction.payment.PaymentMethod;
import com.incognia.transaction.payment.PaymentType;
import com.incognia.transaction.payment.PaymentValue;
import com.incognia.transaction.payment.RegisterPaymentRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class IncogniaAPITest {
  private static final String CLIENT_ID = "client-id";
  private static final String CLIENT_SECRET = "client-secret";
  private MockWebServer mockServer;
  private IncogniaAPI client;

  @BeforeEach
  void setUp() {
    mockServer = new MockWebServer();
    client = new IncogniaAPI(CLIENT_ID, CLIENT_SECRET, mockServer.url("").toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    mockServer.shutdown();
  }

  @Test
  @DisplayName("should return the expected signup response")
  @SneakyThrows
  void testRegisterSignup_whenDataIsValid() {
    String token = TokenCreationFixture.createToken();
    String installationId = "installation-id";
    Address address = AddressFixture.ADDRESS_ADDRESS_LINE;

    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);
    dispatcher.setExpectedAddressLine(address.getAddressLine());
    dispatcher.setExpectedInstallationId(installationId);
    mockServer.setDispatcher(dispatcher);
    RegisterSignupRequest registerSignupRequest =
        RegisterSignupRequest.builder().installationId(installationId).address(address).build();
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

    Reason expectedReason = Reason.builder().code("trusted_location").source("local").build();
    assertThat(signupAssessment.getReasons()).containsExactly(expectedReason);
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterSignup_whenEmptyInstallationId() {
    assertThatThrownBy(
            () ->
                client.registerSignup(
                    RegisterSignupRequest.builder()
                        .installationId("")
                        .address(AddressFixture.ADDRESS_ADDRESS_LINE)
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
  }

  @Test
  @DisplayName("should return the expected signup response")
  @SneakyThrows
  void testGetSignupAssessment_whenDataIsValid() {
    String token = TokenCreationFixture.createToken();
    UUID signupId = UUID.randomUUID();
    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);
    dispatcher.setExpectedSignupId(signupId);
    mockServer.setDispatcher(dispatcher);
    SignupAssessment signupAssessment = client.getSignupAssessment(signupId);
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

    Reason expectedReason = Reason.builder().code("trusted_location").source("local").build();
    assertThat(signupAssessment.getReasons()).containsExactly(expectedReason);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true})
  @NullSource
  @DisplayName("should return the expected login transaction response")
  @SneakyThrows
  void testRegisterLogin_whenDataIsValid(Boolean eval) {
    String token = TokenCreationFixture.createToken();
    String installationId = "installation-id";
    String accountId = "account-id";
    String externalId = "external-id";

    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);
    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .installationId(installationId)
            .externalId(externalId)
            .accountId(accountId)
            .type("login")
            .addresses(null)
            .paymentMethods(null)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterLoginRequest loginRequest =
        RegisterLoginRequest.builder()
            .installationId(installationId)
            .accountId(accountId)
            .externalId(externalId)
            .evaluateTransaction(eval)
            .build();
    TransactionAssessment transactionAssessment = client.registerLogin(loginRequest);
    assertTransactionAssessment(transactionAssessment);
  }

  @Test
  @DisplayName("should return an empty response")
  @SneakyThrows
  void testRegisterLogin_whenEvalIsFalse() {
    String token = TokenCreationFixture.createToken();
    String installationId = "installation-id";
    String accountId = "account-id";
    String externalId = "external-id";

    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);
    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .installationId(installationId)
            .externalId(externalId)
            .accountId(accountId)
            .type("login")
            .addresses(null)
            .paymentMethods(null)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterLoginRequest loginRequest =
        RegisterLoginRequest.builder()
            .installationId(installationId)
            .accountId(accountId)
            .externalId(externalId)
            .evaluateTransaction(false)
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
    String token = TokenCreationFixture.createToken();
    String installationId = "installation-id";
    String accountId = "account-id";
    String externalId = "external-id";
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

    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);
    List<TransactionAddress> transactionAddresses =
        Collections.singletonList(
            new TransactionAddress(
                "shipping", null, address.getStructuredAddress(), address.getCoordinates()));
    RegisterPaymentRequest paymentRequest =
        RegisterPaymentRequest.builder()
            .installationId(installationId)
            .accountId(accountId)
            .externalId(externalId)
            .addresses(Collections.singletonMap(AddressType.SHIPPING, address))
            .evaluateTransaction(eval)
            .paymentValue(paymentValue)
            .paymentMethods(paymentMethods)
            .build();
    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .installationId(installationId)
            .externalId(externalId)
            .accountId(accountId)
            .type("payment")
            .addresses(transactionAddresses)
            .paymentValue(paymentValue)
            .paymentMethods(paymentMethods)
            .build());
    mockServer.setDispatcher(dispatcher);
    TransactionAssessment transactionAssessment = client.registerPayment(paymentRequest);
    assertTransactionAssessment(transactionAssessment);
  }

  @Test
  @DisplayName("should return an empty response")
  @SneakyThrows
  void testRegisterPayment_whenEvalIsFalse() {
    String token = TokenCreationFixture.createToken();
    String installationId = "installation-id";
    String accountId = "account-id";
    String externalId = "external-id";
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

    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);

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
            .installationId(installationId)
            .externalId(externalId)
            .accountId(accountId)
            .type("payment")
            .addresses(transactionAddresses)
            .paymentMethods(paymentMethods)
            .paymentValue(paymentValue)
            .build());
    mockServer.setDispatcher(dispatcher);
    RegisterPaymentRequest paymentRequest =
        RegisterPaymentRequest.builder()
            .installationId(installationId)
            .accountId(accountId)
            .externalId(externalId)
            .addresses(Collections.singletonMap(AddressType.SHIPPING, address))
            .evaluateTransaction(false)
            .paymentValue(paymentValue)
            .paymentMethods(paymentMethods)
            .build();
    TransactionAssessment transactionAssessment = client.registerPayment(paymentRequest);
    assertThat(transactionAssessment).isEqualTo(TransactionAssessment.builder().build());
  }

  @Test
  @DisplayName("should be successful")
  @SneakyThrows
  void testRegisterFeedback_whenDataIsValid() {
    String token = TokenCreationFixture.createToken();
    String installationId = "installation-id";
    String accountId = "account-id";
    String externalId = "external-id";
    String signupId = UUID.randomUUID().toString();
    Instant timestamp = Instant.now();

    TokenAwareDispatcher dispatcher = new TokenAwareDispatcher(token, CLIENT_ID, CLIENT_SECRET);
    dispatcher.setExpectedFeedbackRequestBody(
        PostFeedbackRequestBody.builder()
            .installationId(installationId)
            .externalId(externalId)
            .signupId(signupId)
            .accountId(accountId)
            .event(FeedbackEvent.ACCOUNT_TAKEOVER)
            .timestamp(timestamp.toEpochMilli())
            .build());
    mockServer.setDispatcher(dispatcher);
    client.registerFeedback(
        FeedbackEvent.ACCOUNT_TAKEOVER,
        timestamp,
        FeedbackIdentifiers.builder()
            .installationId(installationId)
            .accountId(accountId)
            .externalId(externalId)
            .signupId(signupId)
            .build());
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterPayment_whenInstallationIdIsNotValid() {
    assertThatThrownBy(
            () ->
                client.registerPayment(
                    RegisterPaymentRequest.builder()
                        .installationId("")
                        .accountId("account id")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
    assertThatThrownBy(
            () ->
                client.registerPayment(
                    RegisterPaymentRequest.builder()
                        .installationId(null)
                        .accountId("account id")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterLogin_whenInstallationIdIsNotValid() {
    assertThatThrownBy(
            () ->
                client.registerLogin(
                    RegisterLoginRequest.builder()
                        .installationId("")
                        .accountId("account id")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
    assertThatThrownBy(
            () ->
                client.registerLogin(
                    RegisterLoginRequest.builder()
                        .installationId(null)
                        .accountId("account id")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterPayment_whenAccountIdIsNotValid() {
    assertThatThrownBy(
            () ->
                client.registerPayment(
                    RegisterPaymentRequest.builder()
                        .installationId("installation-id")
                        .accountId("")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
    assertThatThrownBy(
            () ->
                client.registerPayment(
                    RegisterPaymentRequest.builder()
                        .installationId("installation-id")
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
                        .installationId("installation id")
                        .accountId("")
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
    assertThatThrownBy(
            () ->
                client.registerLogin(
                    RegisterLoginRequest.builder()
                        .installationId("installation id")
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

    Reason expectedReason = Reason.builder().code("trusted_location").source("local").build();
    assertThat(transactionAssessment.getReasons()).containsExactly(expectedReason);
  }
}
