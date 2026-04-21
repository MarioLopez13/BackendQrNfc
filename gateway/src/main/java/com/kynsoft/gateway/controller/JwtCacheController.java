package com.kynsoft.gateway.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.kynsoft.gateway.infrastructure.security.CachingReactiveJwtDecoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actuator/jwt-cache")
public class JwtCacheController {

    private final CachingReactiveJwtDecoder jwtDecoder;

    public JwtCacheController(@org.springframework.beans.factory.annotation.Qualifier("jwtDecoder") CachingReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    @GetMapping("/stats")
    public ResponseEntity<JwtCacheStatsResponse> getStats() {
        CacheStats stats = jwtDecoder.getCacheStats();
        long tokenCacheSize = jwtDecoder.getCacheSize();
        long userInfoCacheSize = jwtDecoder.getUserInfoCacheSize();
        boolean enabled = jwtDecoder.isCacheEnabled();

        JwtCacheStatsResponse response = new JwtCacheStatsResponse(
                enabled,
                tokenCacheSize,
                userInfoCacheSize,
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate(),
                stats.evictionCount(),
                stats.loadSuccessCount(),
                stats.loadFailureCount(),
                stats.averageLoadPenalty() / 1_000_000
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invalidate")
    public ResponseEntity<String> invalidateCache() {
        jwtDecoder.invalidateAll();
        return ResponseEntity.ok("JWT cache y UserInfo cache invalidados correctamente");
    }

    @Getter
    @AllArgsConstructor
    public static class JwtCacheStatsResponse {
        private final boolean enabled;
        private final long tokenCacheSize;
        private final long userInfoCacheSize;
        private final long hitCount;
        private final long missCount;
        private final double hitRate;
        private final long evictionCount;
        private final long loadSuccessCount;
        private final long loadFailureCount;
        private final double averageLoadTimeMs;
    }
}