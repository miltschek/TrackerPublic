package de.miltschek.tracker;

import android.hardware.SensorEvent;

public class StepCounterListener extends SensorListener {
    private static final String TAG = StepCounterListener.class.getSimpleName();
    private ISensorConsumer consumer;

    public StepCounterListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new StepCounterSensorData(event.timestamp, (int)event.values[0], event.accuracy));
    }
}
