package com.example.momo.global.utils;

import com.example.momo.global.utils.dto.LatLngBounds;

/**
 * 위치 관련 유틸리티 클래스
 */
public class LocationUtils {

	// 한국 기준 상수값
	private static final double KM_PER_DEGREE_LAT = 111.0;     // 위도 1도당 km
	private static final double KM_PER_DEGREE_LNG = 88.8;      // 경도 1도당 km (한국 위도 37도 기준)
	private static final double RADIUS_KM = 10.0;              // 반경 10km

	private LocationUtils() {
	}

	/**
	 * 중심점에서 10km 반경의 사각형 영역을 계산
	 *
	 * @param centerLatitude 중심점 위도
	 * @param centerLongitude 중심점 경도
	 * @return 10km 반경의 사각형 경계
	 */
	public static LatLngBounds calculate10KmBounds(double centerLatitude, double centerLongitude) {
		double latOffset = RADIUS_KM / KM_PER_DEGREE_LAT;      // ≈ 0.09
		double lngOffset = RADIUS_KM / KM_PER_DEGREE_LNG;      // ≈ 0.113

		return new LatLngBounds(
			centerLatitude - latOffset,   // 남쪽 (minLat)
			centerLatitude + latOffset,   // 북쪽 (maxLat)
			centerLongitude - lngOffset,  // 서쪽 (minLng)
			centerLongitude + lngOffset   // 동쪽 (maxLng)
		);
	}
}