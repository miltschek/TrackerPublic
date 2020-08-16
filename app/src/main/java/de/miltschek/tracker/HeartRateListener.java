package de.miltschek.tracker;

import android.hardware.SensorEvent;

public class HeartRateListener extends SensorListener {
    private static final String TAG = HeartRateListener.class.getSimpleName();
    private ISensorConsumer consumer;

    public HeartRateListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new HeartRateSensorData(event.timestamp, (int) event.values[0], event.accuracy));
    }
}
