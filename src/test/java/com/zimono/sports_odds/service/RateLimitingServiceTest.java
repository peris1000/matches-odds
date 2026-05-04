package com.zimono.sports_odds.service;

import com.zimono.sports_odds.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private AppProperties properties;

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        properties = new AppProperties();
        properties.getRateLimiting().setSlidingWindow(5);
        properties.getRateLimiting().setMaxRequests(3);

        rateLimitingService = new RateLimitingService(redisTemplate, properties);
        ReflectionTestUtils.setField(rateLimitingService, "appName", "matches-odds");
    }

    @Test
    void isAllowed_shouldReturnTrue_whenRedisReturnsOne() {
        when(redisTemplate.execute(any(), anyBoolean())).thenReturn(1L);

        boolean allowed = rateLimitingService.isAllowed("test-key");
        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowed_shouldReturnFalse_whenRedisReturnsZero() {
        when(redisTemplate.execute(any(), anyBoolean())).thenReturn(0L);

        boolean allowed = rateLimitingService.isAllowed("test-key");
        assertThat(allowed).isFalse();
    }
}
