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

import java.util.List;

/**
 * Interface for accessing sensors' data.
 */
public interface ISensorReadout {
    /** Maximum age of a heart rate value to be considered as valid, 10s. */
    long MAX_HEART_RATE_AGE_NS = 10L * 1000 * 1000 * 1000;

    /**
     * Sets whether a geographical location sensor (GNSS) is to be always active
     * or only during a sport event.
     * @param state true to activate the location server forever,
     *              false to activate it only during sport events
     */
    void setGeoLocationAlwaysActive(boolean state);

    /**
     * Starts a sport activity (measuring the time, collecting sensor data).
     * @param heartRate true if heart rate data shall be collected, false otherwise
     * @param stepCount true if step count data shall be collected, false otherwise
     * @param airPressure true if air pressure data shall be collected, false otherwise
     * @param geoLocation true if geographical location data shall be collected, false otherwise
     */
    void startSportActivity(boolean heartRate, boolean stepCount, boolean airPressure, boolean geoLocation);

    /**
     * Stops a sport activity (measuring the time, collecting sensor data).
     */
    void stopSportActivity();

    /**
     * Returns the duration of the sport activity based on the abstract timestamps in nanoseconds.
     * @return duration of the sport activity in nanoseconds.
     */
    long getSportActivityDurationNs();

    /**
     * Returns the real time clock timestamp of the beginning of the sport activity.
     * @return timestamp of the beginning of the sport activity in milliseconds since Jan, 1st 1970 UTC.
     */
    long getSportActivityStartTimeRtc();

    /**
     * Returns the real time clock timestamp of the end of the sport activity.
     * @return timestamp of the end of the sport activity in milliseconds since Jan, 1st 1970 UTC.
     */
    long getSportActivityStopTimeRtc();

    /**
     * Returns the abstract timestamp of the beginning of the sport activity.
     * @return timestamp of the beginning of the sport activity in nanoseconds since some abstract point in time.
     */
    long getSportActivityStartTimeNs();

    /**
     * Returns the abstract timestamp of the end of the sport activity.
     * @return timestamp of the end of the sport activity in nanoseconds since some abstract point in time.
     */
    long getSportActivityStopTimeNs();

    /**
     * Gets a value indicating whether the sport activity is running (is active).
     * @return true if the sport activity is running, false otherwise.
     */
    boolean isSportActivityRunning();

    /**
     * Resets all data of the last sport activity, if any (timestamps, sensor data).
     */
    void resetSportActivity();

    /**
     * Registers a receiver of sensor data of a specified type.
     * @param clazz type of sensor data to be pushed.
     * @param dataListener receiver of the sensor data.
     * @param <T> type of sensor data to be pushed.
     */
    <T extends SensorData> void registerDataListener(T[] clazz, IDataListener<T> dataListener);

    /**
     * Gets all collected heart rate data events.
     * @return all collected heart rate data events.
     */
    List<? extends HeartRateSensorData> getHeartRateData();

    /**
     * Gets all collected step count data events.
     * @return all collected step count data events.
     */
    List<? extends StepCounterSensorData> getStepData();

    /**
     * Gets all collected geographical location data events.
     * @return all collected geographical location data events.
     */
    List<? extends GeoLocationData> getGeoLocationData();

    /**
     * Gets all collected air pressure data events.
     * @return all collected air pressure data events.
     */
    List<? extends PressureSensorData> getAirPressureData();

    /**
     * Gets an average speed if available or 0 otherwise.
     * @return average speed in meters per seconds.
     */
    float getAvgSpeed();

    /**
     * Gets the current geographical lateral location accuracy or NaN if not available.
     * @return current geographical lateral location accuracy in meters.
     */
    float getGeoAccuracy();

    /**
     * Gets the amount of satellites of the constellation that has the most satellites
     * being received.
     * @return the amount of tracked satellites of the best constellation at the moment
     */
    int getBestSatellitesCount();

    /**
     * Gets a total amount of steps as reported by the steps count sensor
     * or a negative value if unknown.
     * @return total amount of steps as reported by the steps count sensor or a negative value if unknown.
     * TODO it would be better to have the steps since the beginning of the sport activity
     */
    int getTotalStepsCount();

    /**
     * Gets the last known heart rate or a negative value if unknown or too old.
     * The value is considered too old if older than {@link #MAX_HEART_RATE_AGE_NS}.
     * @return last known heart rate or a negative value if unknown or too old.
     */
    int getHeartRate();

    /**
     * Gets the last known position report or null if unknown.
     * @return the last known position report or null if unknown.
     */
    GeoLocationData getLastLocation();
}
