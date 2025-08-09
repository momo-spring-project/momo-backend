package com.example.momo.global.monitoring.query.inspector;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import com.example.momo.global.monitoring.query.request.RequestQueryContext;
import com.example.momo.global.monitoring.query.request.RequestQueryContextHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryCountInspector implements StatementInspector {
	@Override
	public String inspect(String sql) {
		RequestQueryContext context = RequestQueryContextHolder.getContext();
		// 쿼리 횟수 +1
		if (context != null)  context.incrementQueryCount(sql);

		return sql;
	}
}
