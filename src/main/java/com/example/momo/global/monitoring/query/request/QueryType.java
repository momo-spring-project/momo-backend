package com.example.momo.global.monitoring.query.request;

import org.springframework.util.StringUtils;

public enum QueryType {
	SELECT,
	INSERT,
	UPDATE,
	DELETE,
	UNKNOWN;

	public static QueryType from(String sql) {
		if(!StringUtils.hasText(sql)) return UNKNOWN;

		String upperCaseSql = sql.toUpperCase().trim();

		if (upperCaseSql.startsWith(QueryType.SELECT.name())) return QueryType.SELECT;
		if (upperCaseSql.startsWith(QueryType.INSERT.name())) return QueryType.INSERT;
		if (upperCaseSql.startsWith(QueryType.UPDATE.name())) return QueryType.UPDATE;
		if (upperCaseSql.startsWith(QueryType.DELETE.name())) return QueryType.DELETE;
		return QueryType.UNKNOWN;
	}
}