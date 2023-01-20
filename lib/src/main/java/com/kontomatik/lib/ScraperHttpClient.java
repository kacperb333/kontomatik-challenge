package com.kontomatik.lib;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

public interface ScraperHttpClient {
  <T> T post(String url, PostRequest request, BiFunction<Map<String, String>, String, T> jsonResponseWithHeadersHandler) throws IOException;

  record PostRequest(
    Map<String, String> headers,
    Object body
  ) {
  }
}
