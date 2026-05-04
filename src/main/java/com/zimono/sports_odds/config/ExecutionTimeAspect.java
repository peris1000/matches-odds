package com.zimono.sports_odds.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger log = LoggerFactory.getLogger("EXECUTIONS_LOGGER");

    private AppProperties properties;

    @Value("${app.audit.execution-time.enabled:true}")
    private boolean auditEnabled;

    public ExecutionTimeAspect(AppProperties properties) {
        this.properties = properties;
    }

    @Around("@annotation(com.zimono.sports_odds.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.getAudit().getExecutionTime().isEnabled()) {
            return null;
        }
        return logDuration(joinPoint);
    }

    private Object logDuration(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!auditEnabled) {
            return joinPoint.proceed();
        }
        long time = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        time = System.currentTimeMillis() - time;

        log.info("Method [{}] executed in {} ms", joinPoint.getSignature().toShortString(), time);
        return result;
    }

}
