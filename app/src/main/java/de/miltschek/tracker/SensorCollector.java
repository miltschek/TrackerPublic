package de.miltschek.tracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class SensorCollector extends Service implements ISensorReadout, ISensorConsumer {
    private static final String TAG = SensorCollector.class.getSimpleName();

    private GeoLocationListener geoLocationListener = new GeoLocationListener(this);
    private GnssStatusCallback gnssStatusCallback = new GnssStatusCallback();
    private GnssMeasurementsCallback gnssMeasurementsCallback = new GnssMeasurementsCallback();
    private GnssNavigationMessageCallback gnssNavigationMessageCallback = new GnssNavigationMessageCallback();
    private HeartRateListener heartRateListener = new HeartRateListener(this);
    private StepCounterListener stepCounterListener = new StepCounterListener(this);
    private AirPressureListener airPressureListener = new AirPressureListener(this);

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor, mStepCounterSensor, mPressureSensor;
    private LocationManager mLocationManager;
    private final IBinder binder = new LocalBinder();

    private List<HeartRateSensorData> heartRateData = new ArrayList<>();
    private List<StepCounterSensorData> stepData = new ArrayList<>();
    private List<GeoLocationData> geoLocationData = new ArrayList<>();
    private List<PressureSensorData> pressureData = new ArrayList<>();

    private final Object geoSensorStateLock = new Object();
    private boolean mGeoLocationShouldBeActive = false, mIsGeoLocationActive = false, mIsGeoLocationRecorded = false;
    private long startTime, startTimeRtc, stopTime, stopTimeRtc;

    private HeartRateSensorData lastHeartRateData;
    private StepCounterSensorData lastStepCounterData;
    private GeoLocationData lastGeoLocationData;

    public SensorCollector() {
        Log.d(TAG, "Creating a new instance " + hashCode());
    }

    public class LocalBinder extends Binder {
        ISensorReadout getService() {
            return SensorCollector.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // guarantee the service to be restarted in case of a crash
        Log.d(TAG, "onStartCommand " + intent);
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind " + intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind " + intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        deactivateAllSensors(true);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved " + rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind " + intent);
        return binder;
    }

    // ISensorReadout


    @Override
    public void setGeoLocationAlwaysActive(boolean state) {
        synchronized (geoSensorStateLock) {
            if (mGeoLocationShouldBeActive == state) {
                // no changes needed
                return;
            }

            if (state && !mIsGeoLocationActive) {
                // should be on, but it is not (doesn't matter the recording)
                activateGeoSensor();
            } else if (!state && mIsGeoLocationActive && !mIsGeoLocationRecorded) {
                // should be off, but it is on and is not being recorder right now
                deactivateGeoSensor();
            }

            // sync variables
            mGeoLocationShouldBeActive = state;
        }
    }

    @Override
    public void startSportActivity(boolean heartRate, boolean stepCount, boolean airPressure, boolean geoLocation) {
        activateSensors(heartRate, stepCount, airPressure, geoLocation);
        startTime = SystemClock.elapsedRealtimeNanos();
        startTimeRtc = System.currentTimeMillis();
        stopTime = 0;
        stopTimeRtc = 0;
    }

    @Override
    public void stopSportActivity() {
        deactivateAllSensors(false);
        if (stopTime == 0) {
            stopTime = SystemClock.elapsedRealtimeNanos();
            stopTimeRtc = System.currentTimeMillis();
        }
    }

    @Override
    public long getSportActivityDurationNs() {
        if (startTime == 0) {
            return 0;
        } else if (stopTime == 0) {
            return SystemClock.elapsedRealtimeNanos() - startTime;
        } else {
            return stopTime - startTime;
        }
    }

    @Override
    public long getSportActivityStartTimeRtc() {
        return startTimeRtc;
    }

    @Override
    public long getSportActivityStopTimeRtc() {
        return stopTimeRtc;
    }

    @Override
    public long getSportActivityStartTimeNs() {
        return startTime;
    }

    @Override
    public long getSportActivityStopTimeNs() {
        return stopTime;
    }

    @Override
    public boolean isSportActivityRunning() {
        return startTime != 0 && stopTime == 0;
    }

    @Override
    public void resetSportActivity() {
        startTime = 0;
        startTimeRtc = 0;
        stopTime = 0;
        stopTimeRtc = 0;
        synchronized (heartRateData) {
            heartRateData.clear();
        }
        synchronized (stepData) {
            stepData.clear();
        }
        synchronized (geoLocationData) {
            geoLocationData.clear();
        }
        synchronized (pressureData) {
            pressureData.clear();
        }
    }

    private List<? extends HeartRateSensorData> getHeartRateData(int startFromIndex) {
        List<HeartRateSensorData> copy = new ArrayList<>();
        try {
            synchronized (heartRateData) {
                Iterator<HeartRateSensorData> iterator = heartRateData.listIterator(startFromIndex);
                while (iterator.hasNext()) {
                    copy.add(iterator.next());
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, "Requested data elements of of range " + startFromIndex + " while having (not-synced) " + heartRateData.size());
        }

        return copy;
    }

    @Override
    public List<? extends HeartRateSensorData> getHeartRateData() {
        return getHeartRateData(0);
    }

    private List<? extends GeoLocationData> getGeoLocationData(int startFromIndex) {
        List<GeoLocationData> copy = new ArrayList<>();
        try {
            synchronized (geoLocationData) {
                Iterator<GeoLocationData> iterator = geoLocationData.listIterator(startFromIndex);
                while (iterator.hasNext()) {
                    copy.add(iterator.next());
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, "Requested data elements of of range " + startFromIndex + " while having (not-synced) " + geoLocationData.size());
        }

        return copy;
    }

    @Override
    public List<? extends GeoLocationData> getGeoLocationData() {
        return getGeoLocationData(0);
    }

    private List<? extends StepCounterSensorData> getStepData(int startFromIndex) {
        List<StepCounterSensorData> copy = new ArrayList<>();
        try {
            synchronized (stepData) {
                Iterator<StepCounterSensorData> iterator = stepData.listIterator(startFromIndex);
                while (iterator.hasNext()) {
                    copy.add(iterator.next());
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, "Requested data elements of of range " + startFromIndex + " while having (not-synced) " + stepData.size());
        }

        return copy;
    }

    @Override
    public List<? extends StepCounterSensorData> getStepData() {
        return getStepData(0);
    }

    private List<? extends PressureSensorData> getAirPressureData(int startFromIndex) {
        List<PressureSensorData> copy = new ArrayList<>();
        try {
            synchronized (pressureData) {
                Iterator<PressureSensorData> iterator = pressureData.listIterator(startFromIndex);
                while (iterator.hasNext()) {
                    copy.add(iterator.next());
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, "Requested data elements of of range " + startFromIndex + " while having (not-synced) " + pressureData.size());
        }

        return copy;
    }

    @Override
    public List<? extends PressureSensorData> getAirPressureData() {
        return getAirPressureData(0);
    }

    private Collection<IDataListener<? extends SensorData>> airPressureListeners = new HashSet<>(),
        geoLocationListeners = new HashSet<>(),
        heartRateListeners = new HashSet<>(),
        stepCounterListeners = new HashSet<>();

    @Override
    public <T extends SensorData> void registerDataListener(T[] clazz, IDataListener<T> dataListener) {
        Class requestedClass = clazz.getClass().getComponentType();
        if (requestedClass.isAssignableFrom(PressureSensorData.class)) {
            synchronized (airPressureListeners) {
                airPressureListeners.add(dataListener);
                Log.i(TAG, "Total air pressure listeners " + airPressureListeners.size());
            }
        } else if (requestedClass.isAssignableFrom(GeoLocationData.class)) {
            synchronized (geoLocationListeners) {
                geoLocationListeners.add(dataListener);
                Log.i(TAG, "Total geo location listeners " + geoLocationListeners.size());
            }
        } else if (requestedClass.isAssignableFrom(HeartRateSensorData.class)) {
            synchronized (heartRateListeners) {
                heartRateListeners.add(dataListener);
                Log.i(TAG, "Total heart rate listeners " + heartRateListeners.size());
            }
        } else if (requestedClass.isAssignableFrom(StepCounterSensorData.class)) {
            synchronized (stepCounterListeners) {
                stepCounterListeners.add(dataListener);
                Log.i(TAG, "Total step counter listeners " + stepCounterListeners.size());
            }
        } else {
            Log.e(TAG, "Can't register a listener for an unsupported class " + requestedClass.getName());
        }
    }

    @Override
    public float getAvgSpeed() {
        return geoLocationListener.getAvgSpeed();
    }

    @Override
    public float getGeoAccuracy() {
        return geoLocationListener.getLastPositionAccuracy();
    }

    @Override
    public int getBestSatellitesCount() {
        return gnssStatusCallback.getBestSatellitesCount();
    }

    @Override
    public int getHeartRate() {
        HeartRateSensorData data = lastHeartRateData;
        if (data == null || data.getTimestamp() < SystemClock.elapsedRealtimeNanos() - MAX_HEART_RATE_AGE_NS) {
            return -1;
        } else {
            return data.getHeartRate();
        }
    }

    @Override
    public int getTotalStepsCount() {
        StepCounterSensorData data = lastStepCounterData;
        return data == null ? -1 : data.getStepsCount();
    }

    @Override
    public GeoLocationData getLastLocation() {
        return lastGeoLocationData;
    }

    // ISensorConsumer

    @Override
    public void addData(SensorData data) {
        if (data instanceof HeartRateSensorData) {
            synchronized (heartRateData) {
                heartRateData.add((HeartRateSensorData)data);
            }

            synchronized (heartRateListeners) {
                for (IDataListener listener : heartRateListeners) {
                    listener.onDataReceived(data);
                }
            }

            lastHeartRateData = (HeartRateSensorData)data;
        } else if (data instanceof StepCounterSensorData) {
            synchronized (stepData) {
                stepData.add((StepCounterSensorData)data);
            }

            synchronized (stepCounterListeners) {
                for (IDataListener listener : stepCounterListeners) {
                    listener.onDataReceived(data);
                }
            }

            lastStepCounterData = (StepCounterSensorData)data;
        } else if (data instanceof GeoLocationData) {
            if (mIsGeoLocationRecorded) {
                synchronized (geoLocationData) {
                    geoLocationData.add((GeoLocationData) data);
                }
            }

            synchronized (geoLocationListeners) {
                for (IDataListener listener : geoLocationListeners) {
                    listener.onDataReceived(data);
                }
            }

            lastGeoLocationData = (GeoLocationData)data;
        } else if (data instanceof PressureSensorData) {
            synchronized (pressureData) {
                pressureData.add((PressureSensorData)data);
            }

            synchronized (airPressureListeners) {
                for (IDataListener listener : airPressureListeners) {
                    listener.onDataReceived(data);
                }
            }
        } else {
            Log.w(TAG, "Unsupported sensor data type of " + data.getClass().getName());
        }
    }

    // internal implementation

    private void activateGeoSensor() {
        synchronized (geoSensorStateLock) {
            Log.d(TAG, "Activating the geo location receiver.");

            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }

            boolean gnssProviderAvailable = false;
            for (String provider : mLocationManager.getProviders(false)) {
                Log.d(TAG, "Geo location provider found " + provider);
                if (LocationManager.GPS_PROVIDER.equals(provider)) {
                    gnssProviderAvailable = true;
                }
            }

            if (gnssProviderAvailable) {
                try {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, geoLocationListener);
                    mLocationManager.registerGnssStatusCallback(gnssStatusCallback);
                    mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsCallback);
                    mLocationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageCallback);

                    mIsGeoLocationActive = true;
                } catch (SecurityException ex) {
                    Log.e(TAG, "Access denied to the GNSS receiver.", ex);
                }
            } else {
                Log.e(TAG, "No GNSS provider found.");
            }
        }
    }

    private void deactivateGeoSensor() {
        synchronized (geoSensorStateLock) {
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(geoLocationListener);
                mLocationManager.unregisterGnssStatusCallback(gnssStatusCallback);
                mLocationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsCallback);
                mLocationManager.unregisterGnssNavigationMessageCallback(gnssNavigationMessageCallback);

                mIsGeoLocationActive = false;
            }
        }
    }

    private void activateSensors(boolean heartRate, boolean stepCount, boolean airPressure, boolean geoLocation) {
        if (mSensorManager == null) {
            mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        }

        if (heartRate) {
            Log.d(TAG, "Activating the heart rate senor.");

            if (mHeartRateSensor == null) {
                mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            }

            if (mHeartRateSensor == null) {
                Log.e(TAG, "No access to the heart rate sensor.");
            } else {
                mSensorManager.registerListener(heartRateListener, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        if (stepCount) {
            Log.d(TAG, "Activating the step counter.");

            if (mStepCounterSensor == null) {
                mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            }

            if (mStepCounterSensor == null) {
                Log.e(TAG, "No access to the step counter sensor.");
            } else {
                mSensorManager.registerListener(stepCounterListener, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        if (airPressure) {
            Log.d(TAG, "Activating the barometer.");

            if (mPressureSensor == null) {
                mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            }

            if (mPressureSensor == null) {
                Log.e(TAG, "No access to the barometer.");
            } else {
                mSensorManager.registerListener(airPressureListener, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        if (geoLocation) {
            synchronized (geoSensorStateLock) {
                if (!mIsGeoLocationActive) {
                    activateGeoSensor();
                }

                mIsGeoLocationRecorded = true;
            }
        }
    }

    /**
     * Deactivates all sensors by removing all listeners, but those that should work all the time in the background.
     * @param disposing if true deactivate even the background listeners, false keeps the background listeners
     */
    private void deactivateAllSensors(boolean disposing) {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(heartRateListener);
            mSensorManager.unregisterListener(stepCounterListener);
            mSensorManager.unregisterListener(airPressureListener);
        }

        synchronized (geoSensorStateLock) {
            if (disposing || !mGeoLocationShouldBeActive) {
                deactivateGeoSensor();
            }

            mIsGeoLocationRecorded = false;
        }
    }
}
