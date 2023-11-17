# Incognia API Java Client
![test workflow](https://github.com/inloco/incognia-api-java/actions/workflows/test.yaml/badge.svg)
![maven central](https://img.shields.io/maven-central/v/com.incognia/incognia-api-client)

Java lightweight client library for [Incognia APIs](https://dash.incognia.com/api-reference).

## Installation

Incognia API Java Client is available on Maven Central.

### Maven
Add our maven repository
```xml
<repository> 
     <id>incognia</id>
     <url>https://repo.incognia.com/java</url>
</repository>
```

And then download the artifact incognia-api-client
```xml
<dependency>
  <groupId>com.incognia</groupId>
  <artifactId>incognia-api-client</artifactId>
  <version>2.5.1</version>
</dependency>
```

### Gradle
Add our maven repository
```gradle
repositories {
    maven {
        url 'https://repo.incognia.com/java'
    }
}
```

And then add the dependency
```gradle
dependencies {
     implementation 'com.incognia:incognia-api-client:2.5.1'
}
```
We support Java 8+.

## Usage

### Configuration

Before calling the API methods, you need to create an instance of the `IncogniaAPI` class.

```java
IncogniaAPI api = new IncogniaAPI("your-client-id", "your-client-secret");
```
Ideally you should use the instance of IncogniaAPI as a singleton, so that it can properly handle token renewal.

### Incognia API

The implementation is based on the [Incognia API Reference](https://dash.incognia.com/api-reference).

#### Authentication

Authentication is done transparently, so you don't need to worry about it.

If you are curious about how we handle it, you can check the TokenAwareNetworkingClient class

#### Registering Signup

This method registers a new signup for the given installation and address, returning a `SignupAssessment`, containing the risk assessment and supporting evidence:

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret");
try {
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
    RegisterSignupRequest signupRequest = RegisterSignupRequest.builder()
        .address(address)
        .installationId("installation id")
        .build();
     SignupAssessment assessment = api.registerSignup(signupRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Getting a Signup

This method allows you to query the latest assessment for a given signup event, returning a `SignupAssessment`, containing the risk assessment and supporting evidence:

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret");
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
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret");
try {
     RegisterLoginRequest registerLoginRequest =
        RegisterLoginRequest.builder()
          .installationId("installation id")
          .accountId("account id")
          .externalId("external id")
          .evaluateTransaction(true) // can be omitted as it uses true as the default value
          .build();
     TransactionAssessment assessment = api.registerLogin(registerLoginRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Registering Web Login

This method registers a new web login for the given installation and account, returning a `TransactionAssessment`, containing the risk assessment and supporting evidence.

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret");
try {
     RegisterLoginRequest registerLoginRequest =
        RegisterLoginRequest.builder()
          .accountId("account id")
          .externalId("external id")
          .sessionToken("session-token")
          .evaluateTransaction(true) // can be omitted as it uses true as the default value
          .build();
     TransactionAssessment assessment = api.registerWebLogin(registerLoginRequest);
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
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret");
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
     
     List<PaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(
            PaymentMethod.builder()
                .creditCardInfo(
                    CardInfo.builder()
                        .bin("123456")
                        .expiryMonth("10")
                        .expiryYear("2028")
                        .lastFourDigits("4321")
                        .build())
                .type(PaymentType.CREDIT_CARD)
                .build());
        
     RegisterPaymentRequest registerPaymentRequest =
         RegisterPaymentRequest.builder()
             .installationId( "installation-id")
             .accountId("account-id")
             .externalId("external-id")
             .addresses(addresses)
             .evaluateTransaction(true) // can be omitted as it uses true as the default value
             .paymentValue(PaymentValue.builder().currency("BRL").amount(10.0).build())
             .paymentMethods(paymentMethods)
             .build();
    
     TransactionAssessment assessment = api.registerPayment(registerPaymentRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

### Registering Payment or Login without evaluating its risk assessment

Turning off the risk assessment evaluation allows you to register a new transaction (Login or Payment), but the response (`TransactionAssessment`) will be empty. For instance, if you're using the risk assessment only for some payment transactions, you should still register all the other ones: this will avoid any bias on the risk assessment computation.

To register a login or a payment without evaluating its risk assessment, you should use the `evaluateTransaction` boolean set to false

Example:


```java
RegisterLoginRequest registerLoginRequest =
        RegisterLoginRequest.builder()
          .installationId("installation id")
          .accountId("account id")
          .externalId("external id")
          .evaluateTransaction(false)
          .build();
```
Would return an empty risk assessment response:

``{}``

#### Sending Feedback

This method registers a feedback event for the given identifiers (represented in `FeedbackIdentifiers`) related to a signup, login or payment.

```java
IncogniaAPI api = new IncogniaAPI("client-id", "client-secret");
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
