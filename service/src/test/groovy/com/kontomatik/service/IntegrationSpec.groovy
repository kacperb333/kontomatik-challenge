package com.kontomatik.service


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.MongoDBContainer
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
class IntegrationSpec extends Specification {

  @LocalServerPort
  int port

  RestTemplate restTemplate = new RestTemplate()

  private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.3")

  @Autowired
  MongoTemplate mongoTemplate

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongo::getConnectionString)
  }

  def setupSpec() {
    mongo.start()
  }

  def cleanupSpec() {
    mongo.stop()
  }

  def setup() {
    dropAllCollections()
  }

  private void dropAllCollections() {
    mongoTemplate.collectionNames.forEach { mongoTemplate.dropCollection(it) }
  }

  protected HttpResponseWrapper post(
    Map<String, String> headers,
    String url,
    String request = ""
  ) {
    return handleRestClientResponseExceptions({
      restTemplate.exchange("http://localhost:$port/$url", HttpMethod.POST, new HttpEntity(request, httpHeaders(headers)), String.class)
    })
  }

  protected HttpResponseWrapper get(
    Map<String, String> headers,
    String url
  ) {
    return handleRestClientResponseExceptions({
      restTemplate.exchange("http://localhost:$port/$url", HttpMethod.GET, new HttpEntity(httpHeaders(headers)), String.class)
    })
  }

  private HttpResponseWrapper handleRestClientResponseExceptions(Closure<ResponseEntity<String>> closure) {
    try {
      ResponseEntity response = closure()
      return new HttpResponseWrapper(response.statusCode, response.headers, response.body)
    } catch (RestClientResponseException e) {
      return new HttpResponseWrapper(e.statusCode, e.responseHeaders, e.responseBodyAsString)
    }
  }

  protected HttpHeaders httpHeaders(Map<String, String> toAdd) {
    HttpHeaders headers = new HttpHeaders()
    toAdd.entrySet().forEach { headers.add(it.key, it.value) }
    return headers
  }
}

