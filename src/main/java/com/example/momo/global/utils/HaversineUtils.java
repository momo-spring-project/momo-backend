package com.example.momo.global.utils;

public class HaversineUtils {

	private static final int EARTH_RADIUS = 6371000;
	private static final double DEFAULT_THRESHOLD_METERS = 10.0;

	private HaversineUtils() {}

	public static double haversine(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);

		double a = Math.sin(dLat/2) * Math.sin(dLat/2)
			+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
			* Math.sin(dLng/2) * Math.sin(dLng/2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		return EARTH_RADIUS * c;
	}

	public static boolean isInDistance(double lat1, double lng1, double lat2, double lng2) {

		return isInDistance(lat1, lng1, lat2, lng2, DEFAULT_THRESHOLD_METERS);
	}

	public static boolean isInDistance(double lat1, double lng1, double lat2, double lng2, double threshold) {

		double distance = haversine(lat1, lng1, lat2, lng2);

		return distance < threshold;
	}
}