package com.kontomatik.lib;

import com.google.gson.JsonElement;

import java.util.Map;

public interface HttpClient {
  Response post(String url, PostRequest request);

  record PostRequest(
    Map<String, String> headers,
    Object body
  ) {
  }

  interface Response {
    String getHeader(String headerName);

    String extractString(String path);

    Map<String, JsonElement> extractMap(String... path);
  }
}
