package com.example.momo.domain.meetings;

public class Haversine {

	private static final int EARTH_RADIUS = 6371000;
	private static final double THRESHOLD_METERS = 10.0;

	private Haversine() {}

	public static double haversine(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);

		double a = Math.sin(dLat/2) * Math.sin(dLat/2)
			+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
			* Math.sin(dLng/2) * Math.sin(dLng/2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		return EARTH_RADIUS * c;
	}

	public static boolean inDistance(double lat1, double lng1, double lat2, double lng2) {

		double distance = haversine(lat1, lng1, lat2, lng2);

		return distance < THRESHOLD_METERS;
	}
}