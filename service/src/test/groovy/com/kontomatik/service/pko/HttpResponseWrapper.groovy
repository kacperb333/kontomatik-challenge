package com.kontomatik.service.pko

import groovy.json.JsonSlurper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode

class HttpResponseWrapper {
  final HttpStatusCode statusCode
  final HttpHeaders headers
  final String body

  HttpResponseWrapper(HttpStatusCode statusCode, HttpHeaders headers, String body) {
    this.statusCode = statusCode
    this.headers = headers
    this.body = body
  }

  Object slurped() {
    return new JsonSlurper().parseText(body)
  }
}
