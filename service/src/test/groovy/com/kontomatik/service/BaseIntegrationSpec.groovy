package com.kontomatik.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
abstract class BaseIntegrationSpec extends Specification {

  @LocalServerPort
  int servicePort

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
}

