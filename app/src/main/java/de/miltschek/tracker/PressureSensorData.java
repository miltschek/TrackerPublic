package de.miltschek.tracker;

public class PressureSensorData extends SensorData {
    private float pressure;

    public PressureSensorData(long timestamp, float pressure, int accuracy) {
        super(timestamp, accuracy);
        this.pressure = pressure;
    }

    public float getPressure() {
        return pressure;
    }
}
