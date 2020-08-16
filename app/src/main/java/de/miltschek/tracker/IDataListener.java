package de.miltschek.tracker;

public interface IDataListener<T extends SensorData> {
    void onDataReceived(T data);
}
