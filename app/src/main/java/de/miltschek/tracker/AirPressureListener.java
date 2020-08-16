package de.miltschek.tracker;

import android.hardware.SensorEvent;

public class AirPressureListener extends SensorListener {
    private static final String TAG = AirPressureListener.class.getSimpleName();
    private static final float PRESSURE_THRESHOLD = 0.25f;
    private ISensorConsumer consumer;
    //private float lastPressure;

    public AirPressureListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new PressureSensorData(event.timestamp, event.values[0], event.accuracy));
    }
}
