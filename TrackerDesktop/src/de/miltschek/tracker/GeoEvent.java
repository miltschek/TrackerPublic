/*
 *  MIT License
 *
 *  Copyright (c) 2020 miltschek
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package de.miltschek.tracker;

/**
 * Geo (GNSS) data event.
 */
public class GeoEvent extends SensorEvent {
	private long fixTsNs;
	private long fixRtcTime;
	private double latitude;
	private double longitude;
	private float lateralAccuracy;
	private double altitude;
	private float bearing;
	private float speed;

	/**
	 * Creates the geo data event.
	 * @param ts abstract timestamp (ns)
	 * @param fixTsNs abstract timestamp of the fix (ns)
	 * @param fixRtcTime real time clock timestamp of the fix (ms since Jan, 1st 1970)
	 * @param latitude latitude (deg)
	 * @param longitude longitude (deg)
	 * @param accuracy accuracy of the lateral position (m)
	 * @param altitude altitude above the WGS 84 reference ellipsoid (m)
	 * @param bearing bearing (deg)
	 * @param speed speed (m/s)
	 * @param sensorAccuracy sensor accuracy
	 */
	public GeoEvent(long ts, long fixTsNs, long fixRtcTime, double latitude, double longitude, float lateralAccuracy, double altitude,
			float bearing, float speed, int sensorAccuracy) {
		super(ts, sensorAccuracy);
		this.fixTsNs = fixTsNs;
		this.fixRtcTime = fixRtcTime;
		this.latitude = latitude;
		this.longitude = longitude;
		this.lateralAccuracy = lateralAccuracy;
		this.altitude = altitude;
		this.bearing = bearing;
		this.speed = speed;
	}

	/**
	 * Returns the abstract timestamp of the fix.
	 * @return abstract timestamp of the fix (ns)
	 */
	public long getFixTsNs() {
		return fixTsNs;
	}

	/**
	 * Returns the real time clock timestamp of the fix.
	 * @return real time clock timestamp of the fix (ms since Jan, 1st 1970)
	 */
	public long getFixRtcTime() {
		return fixRtcTime;
	}
	
	/**
	 * Returns the latitude.
	 * @return latitude (deg)
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * Returns the longitude.
	 * @return longitude (deg)
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * Returns the accuracy of the lateral position.
	 * @return accuracy of the lateral position (m)
	 */
	public float getLateralAccuracy() {
		return lateralAccuracy;
	}
	
	/**
	 * Returns the altitude above the WGS 84 reference ellipsoid.
	 * @return altitude above the WGS 84 reference ellipsoid (m)
	 */
	public double getAltitude() {
		return altitude;
	}
	
	/**
	 * Returns the bearing.
	 * @return bearing (deg)
	 */
	public float getBearing() {
		return bearing;
	}
	
	/**
	 * Returns the speed.
	 * @return speed (m/s)
	 */
	public float getSpeed() {
		return speed;
	}
}
