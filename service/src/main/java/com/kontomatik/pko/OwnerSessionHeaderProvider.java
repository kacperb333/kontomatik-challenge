package com.kontomatik.pko;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class OwnerSessionHeaderProvider {
    private final String ownerSessionHeaderName;

    public OwnerSessionHeaderProvider(@Value("${service.owner-session-header}") String ownerSessionHeaderName) {
        this.ownerSessionHeaderName = ownerSessionHeaderName;
    }

    public HttpHeaders ownerSessionHeader(OwnerSessionId ownerSessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ownerSessionHeaderName, ownerSessionId.value());
        return headers;
    }
}
