package com.example.momo.global.monitoring.query.request;

public class RequestQueryContextHolder {

	// 스레드 별 독린된 context 저장소
	private static final ThreadLocal<RequestQueryContext> QUERY_CONTEXT = new ThreadLocal<>();

	public static void initContext(RequestQueryContext context) {
		// 이전 컨텍스트가 있을수도 있으니 제거
		QUERY_CONTEXT.remove();

		QUERY_CONTEXT.set(context);
	}

	public static RequestQueryContext getContext() {
		return QUERY_CONTEXT.get();
	}

	public static void clear() {
		// 메모리 누수 방지를 위해 반드시 호출
		QUERY_CONTEXT.remove();
	}

}

