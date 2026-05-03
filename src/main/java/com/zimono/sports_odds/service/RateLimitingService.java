package com.zimono.sports_odds.service;

import com.zimono.sports_odds.config.RateLimitingProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RefreshScope
public class RateLimitingService {

    @Value("${spring.application.name:sports_odds}")
    private String appName;

    private final StringRedisTemplate redisTemplate;
    private final RateLimitingProperties properties;

    public RateLimitingService(StringRedisTemplate redisTemplate, RateLimitingProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public boolean isAllowed(String key) {

        Duration windowSeconds = Duration.ofSeconds(properties.getSlidingWindow());
        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowSeconds.toMillis();

        String redisKey = appName + ":sliding_rate:" + key;

        // Use a Lua script to ensure atomicity
        String luaScript =
                """
                local key = KEYS[1]
                local max = tonumber(ARGV[2])      -- e.g. 3
                local window = tonumber(ARGV[4])   -- e.g. 10 seconds
                
                local current = redis.call('INCR', key)
                if current == 1 then
                    redis.call('EXPIRE', key, window)
                end
                
                -- Return 1 if allowed, 0 if rate limited
                if current <= max then
                    return 1
                else
                    return 0
                end
                """;

        Long allowed = redisTemplate.execute(
                connection -> connection.scriptingCommands().eval(
                        luaScript.getBytes(),
                        ReturnType.INTEGER,
                        1,
                        redisKey.getBytes(),
                        String.valueOf(windowStart).getBytes(),
                        String.valueOf(properties.getMaxRequests()).getBytes(),
                        String.valueOf(now).getBytes(),
                        String.valueOf(windowSeconds.getSeconds()).getBytes()
                ),
                true // expose connection
        );
        return allowed != null && allowed == 1;
    }
}