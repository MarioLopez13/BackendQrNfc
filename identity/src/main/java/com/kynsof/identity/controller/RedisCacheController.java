package com.kynsof.identity.controller;

/*
 * COMENTADO TEMPORALMENTE - Conflicto con el controlador de share
 *
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController("identityRedisCacheController")
@RequestMapping("/api/identity/redis/cache")
@RequiredArgsConstructor
public class RedisCacheController {

    private final LettuceConnectionFactory redisConnectionFactory;
    private final RedissonClient redissonClient;

    @DeleteMapping("/flush-all")
    @PreAuthorize("hasAuthority('ADMIN:INFRAESTRUCTURA')")
    public ResponseEntity<Map<String, Object>> flushAllCache() {
        // ... implementation
    }

    // ... rest of the controller
}
*/
