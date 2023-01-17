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
class AccountsImportRetentionVerifier {
  private static final Logger log = LoggerFactory.getLogger(AccountsImportRetentionVerifier.class);

  private final MongoTemplate mongoTemplate;
  private final long allowedDataRetentionHours;

  AccountsImportRetentionVerifier(
    MongoTemplate mongoTemplate,
    @Value("${accounts-import.retention-hours}") long allowedDataRetentionHours
  ) {
    this.mongoTemplate = mongoTemplate;
    this.allowedDataRetentionHours = allowedDataRetentionHours;
  }

  @PostConstruct
  private void verifyAccountsImportRetention() {
    boolean ttlIndexPresent = mongoTemplate.indexOps(PersistentFinishedSession.class).getIndexInfo().stream()
      .anyMatch(it ->
        isSingleFieldIndex(it) && isIndexForField(it, "createdAt") && isExpireAfter(it, Duration.ofHours(allowedDataRetentionHours))
      );
    if (!ttlIndexPresent) {
      log.error("No retention of accounts import data. Required {} hours of retention.", allowedDataRetentionHours);
    }
  }

  private static boolean isSingleFieldIndex(IndexInfo index) {
    return index.getIndexFields().size() == 1;
  }

  private static boolean isIndexForField(IndexInfo index, String field) {
    return index.getIndexFields().stream().anyMatch(it -> it.getKey().equals(field));
  }

  private static boolean isExpireAfter(IndexInfo index, Duration allowedRetention) {
    return index.getExpireAfter().stream()
      .allMatch(it -> it.equals(allowedRetention));
  }
}
