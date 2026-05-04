package com.zimono.sports_odds.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Security security = new Security();
    private final Caching caching = new Caching();
    private final Audit audit = new Audit();
    private final RateLimiting rateLimiting = new RateLimiting();

    @Getter
    @Setter
    public static class Security {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Caching {
        private boolean enabled = true;
    }


    public static class Audit {
        private final ExecutionTime executionTime = new ExecutionTime();

        public ExecutionTime getExecutionTime() {
            return executionTime;
        }

        @Getter
        @Setter
        public static class ExecutionTime {
            private boolean enabled = true;
        }
    }

    @Getter
    @Setter
    public static class RateLimiting {
        private boolean enabled = true;
        private int maxRequests = 10;
        private int slidingWindow = 60;
    }
}
