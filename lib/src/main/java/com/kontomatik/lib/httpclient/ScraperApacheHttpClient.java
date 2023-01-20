package com.kontomatik.lib.httpclient;

import com.kontomatik.lib.GsonUtils;
import com.kontomatik.lib.ScraperHttpClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ScraperApacheHttpClient implements ScraperHttpClient {
  private static final Logger log = LoggerFactory.getLogger(ScraperApacheHttpClient.class);
  private final HttpClientBuilder apacheClientBuilder;
  private final String baseUrl;

  public ScraperApacheHttpClient(
    String baseUrl,
    HttpClientBuilder apacheClientBuilder
  ) {
    this.baseUrl = baseUrl;
    this.apacheClientBuilder = apacheClientBuilder;
  }

  @Override
  public <T> T post(String url, PostRequest request, BiFunction<Map<String, String>, String, T> jsonResponseWithHeadersHandler) throws IOException {
    HttpPost httpPost = preparePost(url, request);
    return handle(httpPost, jsonResponseWithHeadersHandler);
  }

  private HttpPost preparePost(String url, PostRequest request) throws UnsupportedEncodingException {
    HttpPost httpPost = new HttpPost(baseUrl + url);
    request.headers().forEach(httpPost::addHeader);
    httpPost.setEntity(new StringEntity(GsonUtils.toJson(request.body())));
    return httpPost;
  }

  private <T> T handle(HttpUriRequest httpRequest, BiFunction<Map<String, String>, String, T> jsonResponseWithHeaders) throws IOException {
    try (
      CloseableHttpClient httpClient = apacheClientBuilder.build();
      CloseableHttpResponse httpResponse = httpClient.execute(httpRequest)
    ) {
      String jsonResponseBody = asJson(httpResponse.getEntity());
      Map<String, String> responseHeaders = asNormalizedHeaders(httpResponse.getAllHeaders());
      try {
        return jsonResponseWithHeaders.apply(responseHeaders, jsonResponseBody);
      } catch (Exception e) {
        logErrorRequest(httpRequest, responseHeaders, jsonResponseBody);
        throw e;
      }
    }
  }

  private static Map<String, String> asNormalizedHeaders(Header[] headers) {
    return Arrays.stream(headers)
      .collect(Collectors.toMap((it -> it.getName().toLowerCase()), NameValuePair::getValue));
  }

  private static String asJson(HttpEntity httpEntity) throws IOException {
    return EntityUtils.toString(httpEntity);
  }

  private static void logErrorRequest(HttpUriRequest request, Map<String, String> responseHeaders, String responseBody) {
    String lineSeparator = System.lineSeparator();
    log.error(
      "Failed Request:"
        + lineSeparator
        + String.format("%s : %s", request.getMethod(), request.getURI())
        + lineSeparator
        + "Response Headers:"
        + lineSeparator
        + responseHeaders.toString()
        + lineSeparator
        + "Response Body:"
        + lineSeparator
        + responseBody
    );
  }
}
