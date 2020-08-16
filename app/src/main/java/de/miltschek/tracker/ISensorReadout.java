package de.miltschek.tracker;

import java.util.List;

public interface ISensorReadout {
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
}
