# Incognia API Java Client
![test workflow](https://github.com/inloco/incognia-api-java/actions/workflows/test.yaml/badge.svg)
![maven central](https://img.shields.io/maven-central/v/com.incognia/incognia-api-client)

Java lightweight client library for [Incognia APIs](https://dash.incognia.com/api-reference).

## Installation

Incognia API Java Client is available on Maven Central.

### Maven
```xml
<dependency>
  <groupId>com.incognia</groupId>
  <artifactId>incognia-api-client</artifactId>
  <version>1.1.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.incognia:incognia-api-client:1.1.0'
```
We support Java 8+.

## Usage

### Configuration

Before calling the API methods, you need to create an instance of the `IncogniaAPI` class.

```java
// to use the BR region
IncogniaAPI api = new IncogniaAPI("your-client-id", "your-client-secret", Region.BR);
// to use the US region
IncogniaAPI api = new IncogniaAPI("your-client-id", "your-client-secret", Region.US);
```

### Incognia API

The implementation is based on the [Incognia API Reference](https://dash.incognia.com/api-reference).

#### Authentication

Authentication is done transparently, so you don't need to worry about it.

#### Registering Signup

This method registers a new signup for the given installation and address, returning a `SignupAssessment`, containing the risk assessment and supporting evidence:

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
try {
     Address address = Address address =
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
     SignupAssessment assessment = api.registerSignup("installation id", address);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Getting a Signup

This method allows you to query the latest assessment for a given signup event, returning a `SignupAssessment`, containing the risk assessment and supporting evidence:

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
try {
     UUID signupId = UUID.fromString("c9ac2803-c868-4b7a-8323-8a6b96298ebe");
     SignupAssessment assessment = api.getSignupAssessment(signupId);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Registering Login

This method registers a new login for the given installation and account, returning a `TransactionAssessment`, containing the risk assessment and supporting evidence.
This method also includes some overloads that do not require optional parameters, like `externalId`.

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
try {
     TransactionAssessment assessment = api.registerLogin("installation-id", "account-id", "external-id");
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Registering Payment

This method registers a new payment for the given installation and account, returning a `TransactionAssessment`, containing the risk assessment and supporting evidence.
This method also includes some overloads that do not require optional parameters, like `externalId` and `addresses`.

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
try {
     Address address = Address address =
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
     Map<AddressType, Address> addresses = Map.of(
         AddressType.SHIPPING, address
         AddressType.BILLING, address);
     TransactionAssessment assessment = api.registerPayment("installation-id", "account-id", "external-id");
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Sending Feedback

This method registers a feedback event for the given identifiers (represented in `FeedbackIdentifiers`) related to a signup, login or payment.

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret", Region.BR);
try {
    Instant timestamp = Instant.now();
    client.registerFeedback(
       FeedbackEvent.ACCOUNT_TAKEOVER,
       timestamp,
       FeedbackIdentifiers.builder()
           .installationId("installation-id")
           .accountId("account-id")
           .externalId("external-id")
           .signupId("c9ac2803-c868-4b7a-8323-8a6b96298ebe")
           .build();
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

## Evidences

Every assessment response (`TransactionAssessment` and `SignupAssessment`) includes supporting evidence in a generic `Map<String, Object>`.
You can find all available evidence [here](https://docs.incognia.com/apis/understanding-assessment-evidence#risk-assessment-evidence).

## Exception handling

Every method call can throw `IncogniaAPIException` and `IncogniaException`.

`IncogniaAPIException` is thrown when the API returned an unexpected http status code. You can retrieve it by calling the `getStatusCode` method in the exception,
along with the `getPayload` method, which returns the api response payload, which might include additional details.

`IncogniaException` represents unknown errors, like serialization/deserialization errors.

## How to Contribute

If you have found a bug or if you have a feature request, please report them at this repository issues section.

### Publishing a new version

We use the [Gradle Nexus publish plugin](https://github.com/gradle-nexus/publish-plugin/) to publish artifacts to maven central.

To publish a new version to a staging repository, run:
```bash
./gradlew publishToSonatype closeSonatypeStagingRepository
```
If the version ends with `-SNAPSHOT`, it is published to the Sonatype snapshots repository.

## What is Incognia?

Incognia is a location identity platform for mobile apps that enables:

- Real-time address verification for onboarding
- Frictionless authentication
- Real-time transaction verification

## Create a Free Incognia Account

1. Go to [Incognia](https://www.incognia.com/) and click on "Sign Up For Free"
2. Create an Account
3. You're ready to integrate [Incognia SDK](https://docs.incognia.com/sdk/getting-started) and use [Incognia APIs](https://dash.incognia.com/api-reference)

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)