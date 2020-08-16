package de.miltschek.tracker;

import android.location.Location;

public class GeoLocationData extends SensorData {
    private Location location;

    public GeoLocationData(long timestamp, Location location) {
        super(timestamp, -1);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
