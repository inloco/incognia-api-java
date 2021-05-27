package com.incognia;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;

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

  public SignupResponse registerSignup(String installationId, Address address)
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
        "api/v2/onboarding/signups", postSignupRequestBody, SignupResponse.class);
  }
  // GET onboarding
  // login
  // payment
  // feedback

  private static Map<Region, String> buildApiUrls() {
    HashMap<Region, String> apiUrls = new HashMap<>();
    apiUrls.put(Region.BR, BR_API_URL);
    apiUrls.put(Region.US, US_API_URL);
    return Collections.unmodifiableMap(apiUrls);
  }
}
