package com.example.momo.global.monitoring.query.interceptor;

import org.springframework.stereotype.Component;
import com.example.momo.global.monitoring.query.request.QueryType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.example.momo.global.monitoring.query.request.RequestQueryContext;
import com.example.momo.global.monitoring.query.request.RequestQueryContextHolder;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryCountInterceptor implements HandlerInterceptor {
	public static final String UNKNOWN_PATH = "UNKNOWN_PATH";
	private final MeterRegistry meterRegistry;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		// Context 초기화
		RequestQueryContext context = new RequestQueryContext();
		RequestQueryContextHolder.initContext(context);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
		Exception ex){
		RequestQueryContext context = RequestQueryContextHolder.getContext();

		// HTTP 메서드 추출
		String httpMethod = request.getMethod();

		// 요청 Path 추출
		String bestMatchPath = (String)request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		if(bestMatchPath == null) bestMatchPath = UNKNOWN_PATH;

		// Request 정보 초기화
		context.initRequestContext(httpMethod,bestMatchPath);

		// 쿼리 횟수를 MeterRegistry 에 기록
		if (context != null) {
			context.getQueryCountByType().forEach((
				(queryType, cnt)
					-> recordQueryCount(context, queryType, cnt)));
		}

		// ThreadLocal 해제
		RequestQueryContextHolder.clear();
	}

	private void recordQueryCount(RequestQueryContext context, QueryType queryType, Integer cnt) {
		DistributionSummary summary = DistributionSummary.builder("app.query.per_request")
			.description("요청 별 쿼리 개수")
			.tag("path", context.getPath())
			.tag("http_method", context.getHttpMethod())
			.tag("query_type", queryType.name())
			.publishPercentiles(0.5, 0.95)
			.register(meterRegistry);

		summary.record(cnt);
	}
}
