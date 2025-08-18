package com.example.momo.global.utils.dto;

/**
 * 위도/경도 사각형 영역을 나타내는 클래스
 */
public record LatLngBounds(
	double minLatitude,   // 남쪽 경계
	double maxLatitude,   // 북쪽 경계
	double minLongitude,  // 서쪽 경계
	double maxLongitude   // 동쪽 경계
) {
}