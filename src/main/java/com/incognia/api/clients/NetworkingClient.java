package com.incognia.api.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.incognia.common.exceptions.IncogniaAPIException;
import com.incognia.common.exceptions.IncogniaException;
import java.io.IOException;
import java.io.InterruptedIOException;
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
import org.jetbrains.annotations.NotNull;

public class NetworkingClient {
  private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
  private static final MediaType MEDIA_TYPE_FORM_URLENCODED =
      MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl baseUrl;
  private final MapType mapType;

  public NetworkingClient(OkHttpClient httpClient, String baseUrl) {
    this.httpClient = httpClient;
    this.objectMapper = ObjectMapperFactory.OBJECT_MAPPER;
    this.baseUrl = HttpUrl.parse(baseUrl);
    this.mapType =
        objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
  }

  public <T, U> U doPost(String path, T body, Class<U> responseType, Map<String, String> headers)
      throws IncogniaException {
    return doPost(path, body, responseType, headers, Collections.emptyMap());
  }

  public <T, U> U doPost(
      String path,
      T body,
      Class<U> responseType,
      Map<String, String> headers,
      Map<String, String> queryParameters)
      throws IncogniaException {
    Request request = buildPostRequest(path, body, headers, queryParameters);
    try (Response response = httpClient.newCall(request).execute()) {
      return parseResponse(response, responseType);
    } catch (InterruptedIOException e) {
      throw new IncogniaException("network call timeout", e);
    } catch (IOException e) {
      throw new IncogniaException("network call failed", e);
    }
  }

  public <T> T doPostFormUrlEncoded(
      String path, String body, Class<T> responseType, Map<String, String> headers)
      throws IncogniaException {
    RequestBody requestBody = RequestBody.create(body, MEDIA_TYPE_FORM_URLENCODED);
    Request request =
        new Builder()
            .url(baseUrl.newBuilder().addPathSegments(path).build())
            .post(requestBody)
            .headers(Headers.of(headers))
            .build();
    try (Response response = httpClient.newCall(request).execute()) {
      return parseResponse(response, responseType);
    } catch (InterruptedIOException e) {
      throw new IncogniaException("network call timeout", e);
    } catch (IOException e) {
      throw new IncogniaException("network call failed", e);
    }
  }

  public <T> void doPost(
      String path, T body, Map<String, String> headers, Map<String, String> queryParameters)
      throws IncogniaException {
    Request request = buildPostRequest(path, body, headers, queryParameters);
    try (Response ignored = httpClient.newCall(request).execute()) {
    } catch (InterruptedIOException e) {
      throw new IncogniaException("network call timeout", e);
    } catch (IOException e) {
      throw new IncogniaException("network call failed", e);
    }
  }

  @NotNull
  private <T> Request buildPostRequest(
      String path, T body, Map<String, String> headers, Map<String, String> queryParameters)
      throws IncogniaException {
    HttpUrl.Builder urlBuilder = baseUrl.newBuilder().addPathSegments(path);
    for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
      urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
    }
    Builder requestBuilder = new Builder().url(urlBuilder.build());
    RequestBody requestBody;
    try {
      requestBody =
          body == null
              ? RequestBody.create("", null)
              : RequestBody.create(objectMapper.writeValueAsBytes(body), MEDIA_TYPE_JSON);
    } catch (JsonProcessingException e) {
      throw new IncogniaException("failed writing request body", e);
    }
    return requestBuilder.post(requestBody).headers(Headers.of(headers)).build();
  }

  private <U> U parseResponse(Response response, Class<U> responseType) throws IncogniaException {
    if (!response.isSuccessful()) {
      String payload;
      try (ResponseBody body = response.body()) {
        payload = body.string();
        Map<String, Object> values =
            payload.length() == 0
                ? Collections.emptyMap()
                : objectMapper.readValue(payload, mapType);
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
