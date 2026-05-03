package com.zimono.sports_odds.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@RefreshScope
@ConfigurationProperties(prefix = "app.rate-limiting")
public class RateLimitingProperties {
    private boolean enabled = true;
    private long maxRequests = 3;
    private long slidingWindow = 5;
}
