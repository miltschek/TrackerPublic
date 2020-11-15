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

/**
 * Base class for all sensor events.
 */
public abstract class SensorData {
    private long timestamp;
    private int accuracy;

    /**
     * Creates a sensor event.
     * @param timestamp abstract timestamp of the event (nanoseconds).
     * @param accuracy accuracy as received from the sensor.
     */
    public SensorData(long timestamp, int accuracy) {
        this.timestamp = timestamp;
        this.accuracy = accuracy;
    }

    /**
     * Gets the abstract timestamp of the event (nanoseconds).
     * @return abstract timestamp of the event (nanoseconds).
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the accuracy of the sensor.
     * @return accuracy of the sensor.
     */
    public int getAccuracy() {
        return accuracy;
    }
}
