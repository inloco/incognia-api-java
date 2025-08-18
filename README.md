# Incognia API Java Client
![test workflow](https://github.com/inloco/incognia-api-java/actions/workflows/test.yaml/badge.svg)

Java lightweight client library for [Incognia APIs](https://dash.incognia.com/api-reference).

## Installation

Incognia API Java Client is available on Incognia's Maven Repository. We provide 2 artifact ids: `incognia-api-client` and `incognia-api-client-shaded`.
`incognia-api-client-shaded` includes all of our dependencies shaded into a single jar, so you don't need to worry about dependency conflicts.

### Maven
Add our maven repository
```xml
<repository> 
     <id>incognia</id>
     <url>https://repo.incognia.com/java</url>
</repository>
```

And then add the artifact `incognia-api-client` **or** `incognia-api-client-shaded` as a dependency:

```xml
<dependency>
  <groupId>com.incognia</groupId>
  <artifactId>incognia-api-client</artifactId>
  <version>3.10.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.incognia</groupId>
  <artifactId>incognia-api-client-shaded</artifactId>
  <version>3.10.0</version>
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
     implementation 'com.incognia:incognia-api-client:3.8.0'
}
```
OR
```gradle
dependencies {
     implementation 'com.incognia:incognia-api-client-shaded:3.8.0'
}
```

We support Java 8+.

## Usage

### Configuration

Before calling the API methods, you need to create an instance of the `IncogniaAPI` class.

```java
IncogniaAPI api = IncogniaAPI.init("your-client-id", "your-client-secret");
```

This will create a instance of the IncogniaAPI class, which will handle token renewal automatically. You should reuse this instance throughout your application.

The IncogniaAPI class implements the Multiton design pattern by maintaining a single instance per unique (client id, client secret) pair.

The library also allow the users to configure the call timeout themselves. This will give them more control over the expected time response. This can be done by calling the init passing the CustomOptions object as a parameter.


```java
IncogniaAPI api = IncogniaAPI.init(
    "your-client-id",
    "your-client-secret",
    CustomOptions.builder()
    .timeoutMillis(2000L)
    .keepAliveSeconds(3000)
    .maxConnections(5)
    .build()
);
```

If no parameter is passed the library will use the default timeout of 10 seconds, 5 minutes of keep alive and 5 max connections.

After calling `init`, you can get the created instance by simply calling `IncogniaAPI.instance()` if only one instance has been created, 
or by calling `IncogniaAPI.instance("your-client-id", "your-client-secret")` to specify which instance should be returned.

If you need to use more than one `clientId`/`clientSecret`, it is recommended to use **only** the 
`IncogniaAPI.instance("your-client-id", "your-client-secret")` to get already created instances.

#### Dependency Injection integration examples

If you use a dependency injection framework, you can create a singleton bean for the `IncogniaAPI` class. Below are some examples using common java frameworks:

Spring Boot:
```java
@Configuration
public class IncogniaAPIConfig {
    @Value("${incognia.client-id}")// change this to your property name
    private String clientId;
    
    @Value("${incognia.client-secret}") //change this to your property name
    private String clientSecret;
    
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public IncogniaAPI incogniaAPI() {
        return IncogniaAPI.init(clientId, clientSecret);
    }
}
```

Micronaut:
```java
@Factory
public class IncogniaAPIFactory {
    @Singleton
    //change the @Value to your property name
    public IncogniaAPI incogniaAPI(@Value("${incognia.client-id}") String clientId,
        @Value("${incognia.client-secret}") String clientSecret) {
        return IncogniaAPI.init(clientId, clientSecret);
    }
}
```

### Incognia API

The implementation is based on the [Incognia API Reference](https://dash.incognia.com/api-reference).

#### Authentication

Authentication is done transparently, so you don't need to worry about it.

If you are curious about how we handle it, you can check the `TokenAwareNetworkingClient` class

#### Registering Signup

This method registers a new signup for the given request token and address, returning a `SignupAssessment`, containing the risk assessment and supporting evidence:

```java
IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
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
        .requestToken("request token")
        .build();
     SignupAssessment assessment = api.registerSignup(signupRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

It's also possible to register a signup without an address:

```java
IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
try {
    RegisterSignupRequest signupRequest = RegisterSignupRequest.builder()
        .requestToken("request token")
        .build();
     SignupAssessment assessment = api.registerSignup(signupRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Registering Web Signup

This method registers a new web signup for the given request token, returning a `SignupAssessment`, containing the risk assessment and supporting evidence:

```java
IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
try {
    RegisterWebSignupRequest webSignupRequest = RegisterWebSignupRequest.builder()
        .requestToken("request token")
        .build();
     SignupAssessment assessment = api.registerWebSignup(webSignupRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Registering Login

This method registers a new login for the given request token and account, returning a `TransactionAssessment`, containing the risk assessment and supporting evidence.
This method also includes some overloads that do not require optional parameters, like `externalId`.

```java
IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
try {
     RegisterLoginRequest registerLoginRequest =
        RegisterLoginRequest.builder()
          .requestToken("request token")
          .accountId("account id")
          .externalId("external id")
          .evaluateTransaction(true) // can be omitted as it uses true as the default value
          .customProperties(Map.of(
            "custom-property-key", "custom-property-value",
            "custom-double-property-key", 1.0))
          .build();
     TransactionAssessment assessment = api.registerLogin(registerLoginRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Registering Web Login

This method registers a new web login for the given request token and account, returning a `TransactionAssessment`, containing the risk assessment and supporting evidence.

```java
IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
try {
     RegisterWebLoginRequest webLoginRequest =
        RegisterWebLoginRequest.builder()
          .accountId("account id")
          .externalId("external id")
          .requestToken("request-token")
          .evaluateTransaction(true) // can be omitted as it uses true as the default value
          .build();
     TransactionAssessment assessment = api.registerWebLogin(webLoginRequest);
} catch (IncogniaAPIException e) {
     //Some api error happened (invalid data, invalid credentials)
} catch (IncogniaException e) {
     //Something unexpected happened
}
```

#### Registering Payment

This method registers a new payment for the given request token and account, returning a `TransactionAssessment`, containing the risk assessment and supporting evidence.
This method also includes some overloads that do not require optional parameters, like `externalId` and `addresses`.

```java
IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
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
             .requestToken( "request-token")
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
          .requestToken("request token")
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
IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
try {
    Instant timestamp = Instant.now();
    client.registerFeedback(
       FeedbackEvent.ACCOUNT_TAKEOVER,
       timestamp,
       FeedbackIdentifiers.builder()
           .requestToken("request-token")
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

## What is Incognia?

Incognia is a location identity platform for mobile apps that enables:

- Real-time address verification for onboarding
- Frictionless authentication
- Real-time transaction verification

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
