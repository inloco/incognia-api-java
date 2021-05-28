package com.incognia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.incognia.fixtures.AddressFixture;
import com.incognia.fixtures.TokenCreationFixture;
import java.io.IOException;
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
    SignupAssessment signupAssessment = client.registerSignup(installationId, address);
    assertThat(signupAssessment)
        .extracting("id", "requestId", "riskAssessment")
        .containsExactly(
            UUID.fromString("5e76a7ca-577c-4f47-a752-9e1e0cee9e49"),
            UUID.fromString("8afc84a7-f1d4-488d-bd69-36d9a37168b7"),
            Assessment.LOW_RISK);
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
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterSignup_whenEmptyInstallationId() {
    assertThatThrownBy(() -> client.registerSignup("", AddressFixture.ADDRESS_ADDRESS_LINE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterSignup_whenNullInstallationId() {
    assertThatThrownBy(() -> client.registerSignup(null, AddressFixture.ADDRESS_ADDRESS_LINE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterSignup_whenNullAddress() {
    assertThatThrownBy(() -> client.registerSignup("installation-id", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'address' cannot be null");
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
        .extracting("id", "requestId", "riskAssessment")
        .containsExactly(
            UUID.fromString("5e76a7ca-577c-4f47-a752-9e1e0cee9e49"),
            UUID.fromString("8afc84a7-f1d4-488d-bd69-36d9a37168b7"),
            Assessment.LOW_RISK);
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
  }

  @Test
  @DisplayName("should return the expected login transaction response")
  @SneakyThrows
  void testRegisterLogin_whenDataIsValid() {
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
            .build());
    mockServer.setDispatcher(dispatcher);
    TransactionAssessment transactionAssessment =
        client.registerLogin(installationId, accountId, externalId);
    assertTransactionAssessment(transactionAssessment);
  }

  @Test
  @DisplayName("should return the expected payment transaction response")
  @SneakyThrows
  void testRegisterPayment_whenDataIsValid() {
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
    dispatcher.setExpectedTransactionRequestBody(
        PostTransactionRequestBody.builder()
            .installationId(installationId)
            .externalId(externalId)
            .accountId(accountId)
            .type("payment")
            .addresses(transactionAddresses)
            .build());
    mockServer.setDispatcher(dispatcher);
    TransactionAssessment transactionAssessment =
        client.registerPayment(
            installationId,
            accountId,
            externalId,
            Collections.singletonMap(AddressType.SHIPPING, address));
    assertTransactionAssessment(transactionAssessment);
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterPayment_whenInstallationIdIsNotValid() {
    assertThatThrownBy(() -> client.registerPayment(null, "account id"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
    assertThatThrownBy(() -> client.registerPayment("", "account id"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterLogin_whenInstallationIdIsNotValid() {
    assertThatThrownBy(() -> client.registerLogin(null, "account id"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
    assertThatThrownBy(() -> client.registerLogin("", "account id"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'installation id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterPayment_whenAccountIdIsNotValid() {
    assertThatThrownBy(() -> client.registerPayment("installation id", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
    assertThatThrownBy(() -> client.registerPayment("installation id", ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
  }

  @Test
  @DisplayName("should throw illegal argument exception with correct message")
  @SneakyThrows
  void testRegisterLogin_whenAccountIdIsNotValid() {
    assertThatThrownBy(() -> client.registerLogin("installation id", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
    assertThatThrownBy(() -> client.registerLogin("installation id", ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'account id' cannot be empty");
  }

  private void assertTransactionAssessment(TransactionAssessment transactionAssessment) {
    assertThat(transactionAssessment)
        .extracting("id", "riskAssessment")
        .containsExactly(
            UUID.fromString("dfe1f2ff-8f0d-4ce8-aed1-af8435143044"), Assessment.LOW_RISK);
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
  }
}
