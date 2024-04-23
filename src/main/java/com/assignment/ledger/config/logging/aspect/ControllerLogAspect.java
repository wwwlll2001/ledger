package com.assignment.ledger.config.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Controller logging aspect for logging controller operations.
 */
@Slf4j
@Aspect
@Component
//@EnableAspectJAutoProxy
public class ControllerLogAspect extends AbstractLogAspect {

    @Override
    protected void logMethodStart(JoinPoint joinPoint) {
        log.info("[Controller] method: {} start, args: {}", joinPoint.getSignature().getName(), getArgs(joinPoint));
    }

    @Override
    protected void logMethodEnd(JoinPoint joinPoint, Object ret) {
        log.info("[Controller] method end. return: {}", truncLogMessage(ret));
    }

    @Around("within(@org.springframework.stereotype.Controller *)"
            + " || within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerMethodAround(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }
}
