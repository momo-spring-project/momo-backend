package com.example.momo.global.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class EventLoggingAspect {

	@Pointcut("@annotation(com.example.momo.global.common.aop.EventLoggable)")
	public void loggableMethods() {
	}

	@Before("loggableMethods()")
	public void logEvent(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		if (args.length > 0) {
			Object event = args[0];
			log.debug("[이벤트 수신] {} 수신: {}", event.getClass().getSimpleName(), event);
		}
	}
}
