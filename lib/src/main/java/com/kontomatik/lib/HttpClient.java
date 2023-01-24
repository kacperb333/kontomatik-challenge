package com.kontomatik.lib;

import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface HttpClient {
  Response post(String url, PostRequest request);

  class PostRequest {
    private final Map<String, String> headers;
    public final Object body;

    private PostRequest(Map<String, String> headers, Object body) {
      this.headers = headers;
      this.body = body;
    }

    public Map<String, String> headers() {
      return Collections.unmodifiableMap(headers);
    }

    public static class Builder {
      private final Map<String, String> headers = new HashMap<>();
      private Object body;

      private Builder(Map<String, String> headers) {
        this.headers.putAll(headers);
      }

      public static Builder withStandardHeaders() {
        return new Builder(Map.of("accept", "application/json"));
      }

      public Builder withHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
      }

      public Builder withBody(Object body) {
        this.body = body;
        return this;
      }

      public PostRequest build() {
        return new PostRequest(headers, body);
      }
    }
  }

  interface Response {
    String getHeader(String headerName);

    String extractString(String path);

    Map<String, JsonElement> extractMap(String... path);
  }
}
