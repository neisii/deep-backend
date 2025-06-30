package org.example.backendproject.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.backendproject.threadlocal.TraceIdHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect // 공통으로 관리하고 싶은 기능을 담당하는 클래스에 붙이는 이름
public class LogAspect {

    //
    @Pointcut("execution(* org.example.backendproject.board.service.BoardService..*(..)) "
            + "execution(* org.example.backendproject.board.controller..*(..)) ") // service 패키지에 포함된 모든 클래스의 메서드 로깅
    public void method() {}

    // pointcut???
    @Around("method()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();

        try {
            log.info("[{}][TraceID]{} 메서드 호출 시작 : {} ms", "AOP_LOG", TraceIdHolder.get(), methodName);
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("[{}][TraceID]{}  메서드 예외 : {}", "AOP_LOG", TraceIdHolder.get(), e.getMessage());
            return e;

        } finally {
            long end = System.currentTimeMillis();
            log.info("[{}][TraceID]{} {} 메서드 실행 완료 시간 : {} ms", "AOP_LOG", TraceIdHolder.get(), methodName, end - start);
        }
    }
}
