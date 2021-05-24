package com.incognia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.type.MapType;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkingClient {
  private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl baseUrl;
  private final MapType mapType;

  public NetworkingClient(OkHttpClient httpClient, String baseUrl) {
    this.httpClient = httpClient;
    this.objectMapper =
        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    this.baseUrl = HttpUrl.parse(baseUrl);
    this.mapType =
        objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType) throws IncogniaException {
    return doPost(path, body, responseType, Collections.emptyMap());
  }

  public <U> U doPost(String path, Class<U> responseType, Map<String, String> headers)
      throws IncogniaException {
    return doPost(path, null, responseType, headers);
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType, Map<String, String> headers)
      throws IncogniaException {
    Builder requestBuilder = new Builder().url(baseUrl.newBuilder().addPathSegments(path).build());
    RequestBody requestBody;
    try {
      requestBody =
          body == null
              ? RequestBody.create("", null)
              : RequestBody.create(objectMapper.writeValueAsBytes(body), MEDIA_TYPE_JSON);
    } catch (JsonProcessingException e) {
      throw new IncogniaException("failed writing request body", e);
    }
    Request request = requestBuilder.post(requestBody).headers(Headers.of(headers)).build();
    try (Response response = httpClient.newCall(request).execute()) {
      return parseResponse(response, responseType);
    } catch (IOException e) {
      // TODO(rato): handle timeout
      throw new IncogniaException("network call failed", e);
    }
  }

  private <U> U parseResponse(Response response, Class<U> responseType) throws IncogniaException {
    if (!response.isSuccessful()) {
      String payload;
      try (ResponseBody body = response.body()) {
        payload = body.string();
        if (payload.length() == 0) {
          throw new IncogniaAPIException(response.code(), Collections.emptyMap());
        }
        Map<String, Object> values = objectMapper.readValue(payload, mapType);
        throw new IncogniaAPIException(response.code(), values);
      } catch (IOException e) {
        throw new IncogniaException("failed reading response body", e);
      }
    }
    try {
      return objectMapper.readValue(response.body().byteStream(), responseType);
    } catch (IOException e) {
      throw new IncogniaException("failed reading response body", e);
    }
  }
}
