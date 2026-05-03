package com.zimono.sports_odds.service;

import com.zimono.sports_odds.config.RateLimitingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RateLimitingProperties properties;

    @InjectMocks
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        // Since appName is @Value, it might be null in unit test, but let's see.
    }

    @Test
    void isAllowed_shouldReturnTrue_whenRedisReturnsOne() {
//        when(properties.getSlidingWindow()).thenReturn(5L);
//        when(properties.getMaxRequests()).thenReturn(3L);
        when(redisTemplate.execute(any(), anyBoolean())).thenReturn(1L);

        boolean allowed = rateLimitingService.isAllowed("test-key");

        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowed_shouldReturnFalse_whenRedisReturnsZero() {
//        when(properties.getSlidingWindow()).thenReturn(5L);
//        when(properties.getMaxRequests()).thenReturn(3L);
        when(redisTemplate.execute(any(), anyBoolean())).thenReturn(0L);

        boolean allowed = rateLimitingService.isAllowed("test-key");

        assertThat(allowed).isFalse();
    }
}
