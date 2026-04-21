package com.kynsof.identity.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSessionDto {
    private String id;
    private String userId;
    private String username;
    private String ipAddress;
    private Long start;
    private Long lastAccess;
    private Map<String, String> clients;

    public Instant getStartTime() {
        return start != null ? Instant.ofEpochMilli(start) : null;
    }

    public Instant getLastAccessTime() {
        return lastAccess != null ? Instant.ofEpochMilli(lastAccess) : null;
    }
}
