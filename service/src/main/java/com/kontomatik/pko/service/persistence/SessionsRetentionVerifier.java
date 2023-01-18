package com.kontomatik.pko.service.persistence;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
class SessionsRetentionVerifier {
  private static final Logger log = LoggerFactory.getLogger(SessionsRetentionVerifier.class);

  private final MongoTemplate mongoTemplate;
  private final long allowedDataRetentionHours;

  SessionsRetentionVerifier(
    MongoTemplate mongoTemplate,
    @Value("${accounts-import.retention-hours}") long allowedDataRetentionHours
  ) {
    this.mongoTemplate = mongoTemplate;
    this.allowedDataRetentionHours = allowedDataRetentionHours;
  }

  @PostConstruct
  private void verifyAccountsImportRetention() {
    boolean ttlIndexPresent = mongoTemplate.indexOps("sessions").getIndexInfo().stream()
      .anyMatch(it -> isSingleField(it) && isForField(it, "persistedAt") && isExpireAfter(it, Duration.ofHours(allowedDataRetentionHours)));
    if (!ttlIndexPresent) {
      log.error("No retention of stored sessions. Required {} hours of retention.", allowedDataRetentionHours);
    }
  }

  private static boolean isSingleField(IndexInfo index) {
    return index.getIndexFields().size() == 1;
  }

  private static boolean isForField(IndexInfo index, String field) {
    return index.getIndexFields().stream()
      .anyMatch(it -> it.getKey().equals(field));
  }

  private static boolean isExpireAfter(IndexInfo index, Duration allowedRetention) {
    return index.getExpireAfter().stream()
      .allMatch(it -> it.equals(allowedRetention));
  }
}
