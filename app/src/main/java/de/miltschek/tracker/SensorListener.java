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

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Base listener for all sensor events listeners.
 */
public abstract class SensorListener implements SensorEventListener {
    private static final String TAG = HeartRateListener.class.getSimpleName();

    private int accuracy;

    /**
     * Gets the last received sensor accuracy value.
     * @return the last received sensor accuracy value.
     */
    public int getAccuracy() {
        return accuracy;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String strAccuracy;
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                strAccuracy = "high";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                strAccuracy = "medium";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                strAccuracy = "low";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                strAccuracy = "unreliable";
                break;
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
                strAccuracy = "no contact";
                break;
            default:
                strAccuracy = String.valueOf(accuracy);
        }

        Log.i(TAG, sensor.getName() + " " + strAccuracy);
        this.accuracy = accuracy;
    }
}
