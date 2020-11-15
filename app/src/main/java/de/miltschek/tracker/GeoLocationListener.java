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

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

/**
 * Listener of geographical events.
 */
public class GeoLocationListener implements LocationListener {
    private static final String TAG = GeoLocationListener.class.getSimpleName();
    /** Maximum allowed age for the average speed and position accuracy reports, 10s. */
    private static final long MAX_ACCURACY_AGE_NS = 10L * 1000 * 1000 * 1000;
    private ISensorConsumer consumer;
    private float[] lastSpeeds = new float[5];

    private long lastPositionTimestamp;
    private float lastPositionAccuracy;

    /**
     * Creates the listener.
     * @param consumer central receiver of sensor events.
     */
    public GeoLocationListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Gets the average speed, in meters per second, calculated out of the last 5 position reports of the GNSS.
     * If the last position report is older than {@link #MAX_ACCURACY_AGE_NS}, the reported speed
     * will be equal to zero.
     * @return average speed as reported by the GNSS
     */
    public float getAvgSpeed() {
        if (lastPositionTimestamp < SystemClock.elapsedRealtimeNanos() - MAX_ACCURACY_AGE_NS) {
            return 0;
        } else {
            float result = 0;
            for (int n = 0; n < lastSpeeds.length; n++) {
                result += lastSpeeds[n] / lastSpeeds.length;
            }

            return result;
        }
    }

    /**
     * Gets the last reported position accuracy, in meters.
     * If the last position report is older than {@link #MAX_ACCURACY_AGE_NS}, the reported accuracy
     * will be equal to NaN.
     * @return position accuracy or NaN if not available
     */
    public float getLastPositionAccuracy() {
        if (lastPositionTimestamp < SystemClock.elapsedRealtimeNanos() - MAX_ACCURACY_AGE_NS) {
            return Float.NaN;
        } else {
            return lastPositionAccuracy;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoLocationData geoLocationData = new GeoLocationData(SystemClock.elapsedRealtimeNanos(), location);
        consumer.addData(geoLocationData);

        lastPositionTimestamp = location.getElapsedRealtimeNanos();
        lastPositionAccuracy = location.getAccuracy();

        Log.i(TAG, location.getLatitude() + ", " + location.getLongitude() + " +-" + location.getAccuracy() + " " + hashCode());

        for (int n = 1; n < lastSpeeds.length; n++) {
            lastSpeeds[n - 1] = lastSpeeds[n];
        }

        lastSpeeds[lastSpeeds.length - 1] = location.getSpeed();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Provider " + provider + " status changed to " + status + ".");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled " + provider);
    }
}
