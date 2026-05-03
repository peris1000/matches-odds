package com.zimono.sports_odds.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String MATCHES_CACHE = "matches";
    public static final String MATCH_BY_ID_CACHE = "matchById";
    public static final String MATCHES_BY_DATE_AND_TEAMS_CACHE = "matchesByDate";

    public static final String TEAMS_CACHE = "teams";
    public static final String TEAM_BY_ID_CACHE = "teamById";
    public static final String TEAM_FULL_BY_ID_CACHE = "teamFullById";

    @Bean
    @ConditionalOnProperty(name = "app.caching.enabled", havingValue = "true", matchIfMissing = true)
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                MATCHES_CACHE, MATCH_BY_ID_CACHE, MATCHES_BY_DATE_AND_TEAMS_CACHE,
                TEAMS_CACHE, TEAM_BY_ID_CACHE,TEAM_FULL_BY_ID_CACHE
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)  // Auto-evict after 1 min
            .maximumSize(1000)                              // Max 1000 entries
            .recordStats());                                // Enable statistics
        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(name = "app.caching.enabled", havingValue = "false")
    public CacheManager noOpCacheManager() {
        // NoOpCacheManager does nothing – caching is effectively disabled
        return new NoOpCacheManager();

        // you may use ConcurrentMapCacheManager as a simple fallback
        // return new ConcurrentMapCacheManager();
    }

}
