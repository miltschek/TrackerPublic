package de.miltschek.tracker;

public class StepCounterSensorData extends SensorData {
    private int stepsCount;

    public StepCounterSensorData(long timestamp, int stepsCount, int accuracy) {
        super(timestamp, accuracy);
        this.stepsCount = stepsCount;
    }

    public int getStepsCount() {
        return stepsCount;
    }
}
