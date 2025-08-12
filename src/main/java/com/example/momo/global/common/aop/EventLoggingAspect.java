package com.example.momo.global.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class EventLoggingAspect {

	@Pointcut("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
	public void rabbitListenerMethods() {
	}

	@Pointcut("bean(rabbitTemplate) && execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.convertAndSend(..))")
	public void rabbitPublishMethods() {
	}

	@Before("rabbitListenerMethods()")
	public void logBeforeListener(JoinPoint point) {
		String methodName = point.getSignature().toShortString();
		String type = extractType(point.getArgs());

		log.info("[RabbitMQ 리스너 접근] , method={}, type={}", methodName, type);
	}

	@AfterReturning("rabbitPublishMethods()")
	public void logAroundPublish(JoinPoint point) {
		Object[] args = point.getArgs();

		String exchange = args.length > 0 ? String.valueOf(args[0]) : "?";
		String routingKey = args.length > 1 ? String.valueOf(args[1]) : "?";
		String type = extractType(args);

		log.info("[RabbitMQ 퍼블리싱] ex={}, key={}, type={}", exchange, routingKey, type);
	}

	private String extractType(Object[] args) {
		for (Object object : args) {
			if (object instanceof EventWrapper<?> ew) {
				String type = ew.type();
				return (type == null || type.isBlank()) ? "null" : type;
			}
		}
		return "unknown";
	}
}
