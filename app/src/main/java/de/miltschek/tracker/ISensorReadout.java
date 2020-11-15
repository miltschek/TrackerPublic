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

    void setGeoLocationAlwaysActive(boolean state);
    void startSportActivity(boolean heartRate, boolean stepCount, boolean airPressure, boolean geoLocation);
    void stopSportActivity();
    long getSportActivityDurationNs();
    long getSportActivityStartTimeRtc();
    long getSportActivityStopTimeRtc();
    long getSportActivityStartTimeNs();
    long getSportActivityStopTimeNs();
    boolean isSportActivityRunning();
    void resetSportActivity();

    <T extends SensorData> void registerDataListener(T[] clazz, IDataListener<T> dataListener);
    /*List<? extends HeartRateSensorData> getHeartRateData(int startFromIndex);
    List<? extends StepCounterSensorData> getStepData(int startFromIndex);
    List<? extends GeoLocationData> getGeoLocationData(int startFromIndex);
    List<? extends PressureSensorData> getAirPressureData(int startFromIndex);*/
    List<? extends HeartRateSensorData> getHeartRateData();
    List<? extends StepCounterSensorData> getStepData();
    List<? extends GeoLocationData> getGeoLocationData();
    List<? extends PressureSensorData> getAirPressureData();

    float getAvgSpeed();
    float getGeoAccuracy();
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
