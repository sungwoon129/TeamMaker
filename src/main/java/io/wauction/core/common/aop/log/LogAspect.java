package io.wauction.core.common.aop.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LogAspect {

    /**
     * auction 패키지와 channels 패키지 하위의 비즈니스 계층 메소드 수행시간 로깅
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* io.wauction.core.auction.application.*(..)) || execution(* io.wauction.core.channels.application.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint ) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        if(executionTime > 3000) log.warn("{} executed in {} ms", joinPoint.getSignature(), executionTime);
        else log.debug("{} executed in {} ms", joinPoint.getSignature(), executionTime);

        return result;
    }
}
