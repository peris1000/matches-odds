package com.zimono.sports_odds.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/cache")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/stats")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStatis() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("cacheSizes", getCacheSizes());
        response.put("cacheNames", List.of(cacheManager.getCacheNames()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/{cacheName}")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheCounts(@PathVariable String cacheName) {
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        CacheStats stats = cache.getNativeCache().stats();
        return ResponseEntity.ok(Map.of(
                "hitCount", stats.hitCount(),
                "missCount", stats.missCount(),
                "hitRate", stats.hitRate()
        ));
    }

    @GetMapping("/keys/{cacheName}")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<?> getCacheKeys(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            List<Object> list = extractKeysFromCache(cache);
            return ResponseEntity.ok(list);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/evict/{cacheName}")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<String> evictCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok("Cache " + cacheName + " cleared successfully");
        }
        return ResponseEntity.badRequest().body("Cache " + cacheName + " not found");
    }

    @PostMapping("/evict/{cacheName}/{key}")
    @PreAuthorize( "hasRole('ADMIN')")
    public ResponseEntity<String> evictCacheKey(@PathVariable String cacheName,
                                                @PathVariable String key) {

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(Integer.valueOf(key));
            return ResponseEntity.ok("Key " + key + " evicted from cache " + cacheName);
        }
        return ResponseEntity.badRequest().body("Cache " + cacheName + " not found");
    }

    private List<Object> extractKeysFromCache(Cache cache) {
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = ((CaffeineCache) cache).getNativeCache();
        return new ArrayList<>(nativeCache.asMap().keySet());
    }

    private Map<String, Object> getCacheSizes() {
        if (cacheManager == null) {
            return Map.of("enabled", false);
        }
        Map<String, Object> sizes = new HashMap<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = ((CaffeineCache) cache).getNativeCache();
            sizes.put(cacheName + ".size", nativeCache.estimatedSize());
        }
        return sizes;
    }

}
