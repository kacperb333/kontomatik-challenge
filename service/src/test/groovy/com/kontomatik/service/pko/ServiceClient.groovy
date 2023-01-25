package com.kontomatik.service.pko


import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

import static ScraperController.SESSION_HEADER

class ServiceClient {

  final long servicePort
  private RestTemplate restTemplate = new RestTemplate()

  ServiceClient(long servicePort) {
    this.servicePort = servicePort
  }

  HttpResponseWrapper postSignIn(String login, String password) {
    return post(
      [
        "content-type": "application/json"
      ],
      "/session",
      """
        {
          "credentials": {
            "login": "$login",
            "password": "$password"
          }
        }
      """
    )
  }

  HttpResponseWrapper postOtp(String otp, String sessionId) {
    return post(
      [
        "content-type"  : "application/json",
        (SESSION_HEADER): sessionId
      ],
      "/session/otp",
      """
        {
          "otp": {
            "code": "$otp"
          }
        }
      """
    )
  }

  HttpResponseWrapper getAccounts(String sessionId) {
    get(
      [
        (SESSION_HEADER): sessionId
      ],
      "/session/accounts"
    )
  }

  protected HttpResponseWrapper post(
    Map<String, String> headers,
    String url,
    String request = ""
  ) {
    return handleRestClientResponseExceptions {
      restTemplate.exchange("http://localhost:$servicePort/$url", HttpMethod.POST, new HttpEntity(request, httpHeaders(headers)), String.class)
    }
  }

  protected HttpResponseWrapper get(
    Map<String, String> headers,
    String url
  ) {
    return handleRestClientResponseExceptions {
      restTemplate.exchange("http://localhost:$servicePort/$url", HttpMethod.GET, new HttpEntity(httpHeaders(headers)), String.class)
    }
  }

  private static HttpResponseWrapper handleRestClientResponseExceptions(Closure<ResponseEntity<String>> closure) {
    try {
      ResponseEntity response = closure()
      return new HttpResponseWrapper(response.statusCode, response.headers, response.body)
    } catch (RestClientResponseException e) {
      return new HttpResponseWrapper(e.statusCode, e.responseHeaders, e.responseBodyAsString)
    }
  }

  protected static HttpHeaders httpHeaders(Map<String, String> toAdd) {
    HttpHeaders headers = new HttpHeaders()
    toAdd.entrySet().forEach { headers.add(it.key, it.value) }
    return headers
  }

  static String extractSessionId(HttpResponseWrapper response) {
    return response.headers.getFirst(SESSION_HEADER)
  }
}
