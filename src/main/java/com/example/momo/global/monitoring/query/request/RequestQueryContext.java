package com.example.momo.global.monitoring.query.request;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class RequestQueryContext {
	private String httpMethod;
	private String path;
	private final Map<QueryType, Integer> queryCountByType = new HashMap<>();

	public void initRequestContext(String httpMethod, String path) {
		this.httpMethod = httpMethod;
		this.path = path;
	}


	public void incrementQueryCount(String sql) {
		QueryType queryType = QueryType.from(sql);
		queryCountByType.merge(queryType, 1, Integer::sum);
	}
}