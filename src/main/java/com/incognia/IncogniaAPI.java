package com.incognia;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;

public class IncogniaAPI {
  private static final String BR_API_URL = "https://incognia.inloco.com.br";
  private static final String US_API_URL = "https://api.us.incognia.com";
  private static final Map<Region, String> API_URLS = buildApiUrls();

  private final String clientId;
  private final String clientSecret;
  private final String apiUrl;
  private final OkHttpClient httpClient;

  public IncogniaAPI(String clientId, String clientSecret) {
    this(clientId, clientSecret, Region.US);
  }

  public IncogniaAPI(String clientId, String clientSecret, Region region) {
    this(clientId, clientSecret, API_URLS.get(region));
  }

  IncogniaAPI(String clientId, String clientSecret, String apiUrl) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.apiUrl = apiUrl;
    // TODO (rato): set client timeout
    this.httpClient = new OkHttpClient.Builder().build();
  }

  // POST onboarding
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
