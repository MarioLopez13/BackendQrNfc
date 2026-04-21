package com.kynsof.identity.infrastructure.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class QueryMonitoringAspect {

    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Registrar consultas que toman más de 500ms
            if (duration > 500) {
                log.warn("Consulta lenta detectada: {}.{} - duración: {} ms", 
                        className, methodName, duration);
            } else {
                log.debug("Consulta: {}.{} - duración: {} ms", 
                        className, methodName, duration);
            }
        }
    }
}