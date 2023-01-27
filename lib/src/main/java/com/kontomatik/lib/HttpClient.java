package com.kontomatik.lib;

import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface HttpClient {
  Response execute(PostRequest request);

  record PostRequest(
    String url,
    Map<String, String> headers,
    Object body
  ) {
    public Map<String, String> headers() {
      return Collections.unmodifiableMap(headers);
    }

    public static class Builder {
      private String url;
      private final Map<String, String> headers = new HashMap<>();
      private Object body;

      private Builder(Map<String, String> headers) {
        this.headers.putAll(headers);
      }

      public static Builder jsonRequest() {
        return new Builder(Map.of("accept", "application/json"));
      }

      public Builder withHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
      }

      public Builder withUrl(String url) {
        this.url = url;
        return this;
      }

      public Builder withBody(Object body) {
        this.body = body;
        return this;
      }

      public PostRequest build() {
        return new PostRequest(url, headers, body);
      }
    }
  }

  interface Response {
    String getHeader(String headerName);

    String extractString(String path);

    Map<String, JsonElement> extractMap(String... path);
  }
}
