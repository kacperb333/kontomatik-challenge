package com.kontomatik.service

import com.kontomatik.service.common.DateTimeProvider
import groovy.json.JsonSlurper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import spock.lang.Specification

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
abstract class BaseIntegrationSpec extends Specification {

  @LocalServerPort
  int servicePort

  private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.3")

  @Autowired
  MongoTemplate mongoTemplate

  @SpringBean
  DateTimeProvider dateTimeProvider = Stub()

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
    clearAllCollections()
  }

  void stubTimeToNow() {
    dateTimeProvider.now() >> { Instant.now() }
  }

  void stubTimeTo(Instant instant) {
    dateTimeProvider.now() >> instant
  }

  private void clearAllCollections() {
    mongoTemplate.collectionNames.forEach { mongoTemplate.remove(new Query(), it) }
  }

  protected boolean jsonsEqual(String first, String second) {
    JsonSlurper slurper = new JsonSlurper()
    def firstJson = slurper.parseText(first)
    def secondJson = slurper.parseText(second)
    return firstJson == secondJson
  }
}

