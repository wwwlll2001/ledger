package com.assignment.ledger.config.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.Arrays;


/**
 * Abstract log aspect for logging operations transparently
 */
@Slf4j
public abstract class AbstractLogAspect {

    private static final int MAX_LOG_MESSAGE_LENGTH = 1000;

    protected abstract void logMethodStart(JoinPoint joinPoint);

    protected Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        logMethodStart(joinPoint);
        long start = System.currentTimeMillis();
        try {
            Object ret = joinPoint.proceed();
            logEnd(joinPoint, start, null, ret);
            return ret;
        } catch (Throwable e) {
            logEnd(joinPoint, start, e, null);
            throw e;
        }
    }

    protected String truncLogMessage(Object message) {
        if (null == message) {
            return null;
        } else {
            String logMsg = message.toString();
            return logMsg.length() > MAX_LOG_MESSAGE_LENGTH ? logMsg.substring(0, MAX_LOG_MESSAGE_LENGTH) : logMsg;
        }
    }

    protected String getArgs(JoinPoint joinPoint) {
        String arguments = Arrays.toString(joinPoint.getArgs());
        if (arguments.length() > 200) {
            arguments = arguments.substring(0, 200);
        }
        return arguments;
    }

    private void logEnd(ProceedingJoinPoint joinPoint, long startTime, Throwable throwable, Object ret) {
        long costTime = System.currentTimeMillis() - startTime;
        logMethodEnd(joinPoint, ret);
    }



    protected abstract void logMethodEnd(JoinPoint joinPoint, Object ret);

}
