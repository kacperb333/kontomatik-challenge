package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.FinishedSession;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "finished_session")
interface PersistentFinishedSession {
  FinishedSession toDomain();
}
