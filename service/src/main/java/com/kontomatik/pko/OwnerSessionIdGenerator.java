package com.kontomatik.pko;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OwnerSessionIdGenerator {
    OwnerSessionId generate() {
        return new OwnerSessionId(UUID.randomUUID().toString());
    }
}
