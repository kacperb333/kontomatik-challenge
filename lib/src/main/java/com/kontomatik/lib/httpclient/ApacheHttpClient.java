package com.kontomatik.lib.httpclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kontomatik.lib.GsonUtils;
import com.kontomatik.lib.HttpClient;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ApacheHttpClient implements HttpClient {
  private final HttpClientBuilder apacheClientBuilder;
  private final String baseUrl;

  public ApacheHttpClient(
    String baseUrl,
    HttpClientBuilder apacheClientBuilder
  ) {
    this.baseUrl = baseUrl;
    this.apacheClientBuilder = apacheClientBuilder;
  }

  @Override
  public Response post(String url, PostRequest request) {
    return doPost(preparePost(url, request));
  }

  private HttpPost preparePost(String url, PostRequest request) {
    HttpPost httpPost = new HttpPost(baseUrl + url);
    request.headers().forEach(httpPost::addHeader);
    httpPost.setEntity(asStringEntity(request.body));
    return httpPost;
  }

  private Response doPost(HttpUriRequest httpRequest) {
    try (
      CloseableHttpClient httpClient = buildClient();
      CloseableHttpResponse httpResponse = doExecute(httpClient, httpRequest)
    ) {
      return new ApacheHttpResponse(httpResponse.getAllHeaders(), httpResponse.getEntity());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private CloseableHttpClient buildClient() {
    return apacheClientBuilder.build();
  }

  private static CloseableHttpResponse doExecute(CloseableHttpClient httpClient, HttpUriRequest httpUriRequest) {
    return uncheck(() -> httpClient.execute(httpUriRequest));
  }

  private static StringEntity asStringEntity(Object entity) {
    return uncheck(() -> new StringEntity(GsonUtils.toJson(entity)));
  }

  private static <T> T uncheck(Callable<T> toRun) {
    try {
      return toRun.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static class ApacheHttpResponse implements Response {
    private final Map<String, String> headers;
    private final JsonObject jsonEntity;


    ApacheHttpResponse(Header[] headers, HttpEntity httpEntity) {
      this.headers = asNormalizedHeaders(headers);
      this.jsonEntity = asJson(httpEntity);
    }

    private static Map<String, String> asNormalizedHeaders(Header[] headers) {
      return Arrays.stream(headers)
        .collect(Collectors.toMap((it -> it.getName().toLowerCase()), NameValuePair::getValue));
    }

    private static JsonObject asJson(HttpEntity httpEntity) {
      return uncheck(() -> GsonUtils.parseToObject(EntityUtils.toString(httpEntity)));
    }

    @Override
    public String getHeader(String headerName) {
      return headers.get(headerName);
    }

    @Override
    public String extractString(String path) {
      return GsonUtils.extractString(jsonEntity, path);
    }

    @Override
    public Map<String, JsonElement> extractMap(String... path) {
      return GsonUtils.extractMap(jsonEntity, path);
    }
  }

}
