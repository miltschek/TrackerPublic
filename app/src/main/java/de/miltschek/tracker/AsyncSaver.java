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

import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class AsyncSaver extends AsyncTask<ISensorReadout, Float, Integer> {
    private static final String TAG = AsyncSaver.class.getSimpleName();
    private final Consumer<Integer> finishedCallback;
    private final File targetDirectory;

    public AsyncSaver(Consumer<Integer> finishedCallback, File targetDirectory) {
        this.finishedCallback = finishedCallback;
        this.targetDirectory = targetDirectory;
    }

    @Override
    protected Integer doInBackground(ISensorReadout... iSensorReadouts) {
        if (iSensorReadouts == null || iSensorReadouts.length == 0) {
            return 0;
        }

        int succeeded = 0;

        for (ISensorReadout sensorReadout : iSensorReadouts) {
            List<? extends HeartRateSensorData> heartRateSensorData = sensorReadout.getHeartRateData();
            List<? extends StepCounterSensorData> stepCounterSensorData = sensorReadout.getStepData();
            List<? extends GeoLocationData> geoLocationData = sensorReadout.getGeoLocationData();
            List<? extends PressureSensorData> pressureSensorData = sensorReadout.getAirPressureData();

            Log.i(TAG, "No. of events "
                    + heartRateSensorData.size() + " heart, "
                    + stepCounterSensorData.size() + " steps, "
                    + geoLocationData.size() + " geo, "
                    + pressureSensorData.size() + " pressure.");

            try {
                FileOutputStream fos = new FileOutputStream(new File(targetDirectory, System.currentTimeMillis() + ".trk"));
                fos.write(FileItem.HEADER); // header
                fos.write(BitUtility.getBytes(FileItem.VERSION)); // version

                FileItem.writeField(fos, BitUtility.getBytes((short)0x1001), BitUtility.getBytes(sensorReadout.getSportActivityStartTimeRtc()));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1002), BitUtility.getBytes(sensorReadout.getSportActivityStopTimeRtc()));
                long startTimestampNs = sensorReadout.getSportActivityStartTimeNs();
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1003), BitUtility.getBytes(startTimestampNs));
                long stopTimestampNs = sensorReadout.getSportActivityStopTimeNs();
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1004), BitUtility.getBytes(stopTimestampNs));

                // create a few statistics for faster lookup
                // average and maximum heart rate
                float avgHeartRate = 0;
                int maxHeartRate = 0;
                int countHeartRate = 0;
                for (HeartRateSensorData data : heartRateSensorData) {
                    if (data.getTimestamp() >= startTimestampNs && data.getTimestamp() <= stopTimestampNs && data.getAccuracy() >= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                        avgHeartRate = avgHeartRate * ((float)countHeartRate / (++countHeartRate)) + (float)data.getHeartRate() / countHeartRate;
                        if (data.getHeartRate() > maxHeartRate) {
                            maxHeartRate = data.getHeartRate();
                        }
                    }
                }

                FileItem.writeField(fos, BitUtility.getBytes((short)0x1011), BitUtility.getBytes(avgHeartRate));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1012), BitUtility.getBytes(maxHeartRate));

                // steps per minute in average
                int startStepsValue = -1;
                int stopStepsValue = -1;
                float avgStepsRate = 0;
                for (StepCounterSensorData data : stepCounterSensorData) {
                    if (data.getTimestamp() >= startTimestampNs && data.getTimestamp() <= stopTimestampNs && data.getAccuracy() >= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                        if (startStepsValue < 0) {
                            startStepsValue = data.getStepsCount();
                        }

                        stopStepsValue = data.getStepsCount();
                    }
                }

                avgStepsRate = (stopStepsValue - startStepsValue) / ((stopTimestampNs - startTimestampNs) / 1000 / 1000 / 1000 / 60f);
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1013), BitUtility.getBytes(stopStepsValue - startStepsValue));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1014), BitUtility.getBytes(avgStepsRate));

                // total ascent und descent, average speed as of the GNSS
                boolean firstAltitude = true;
                double lastAltitude = 0;
                double totalAscent = 0;
                double totalDescent = 0;
                float avgSpeed = 0;
                int countSpeed = 0;
                for (GeoLocationData data : geoLocationData) {
                    if (data.getTimestamp() >= startTimestampNs && data.getTimestamp() <= stopTimestampNs) {
                        Location location = data.getLocation();

                        if (firstAltitude) {
                            firstAltitude = false;
                        } else {
                            double diff = location.getAltitude() - lastAltitude;
                            if (diff > 0) {
                                totalAscent += diff;
                            } else {
                                totalDescent -= diff;
                            }
                        }

                        lastAltitude = location.getAltitude();

                        avgSpeed = avgSpeed * ((float)countSpeed / (++countSpeed)) + location.getSpeed() / countSpeed;
                    }
                }

                FileItem.writeField(fos, BitUtility.getBytes((short)0x1015), BitUtility.getBytes((float)totalAscent));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1016), BitUtility.getBytes((float)totalDescent));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1017), BitUtility.getBytes(avgSpeed));

                // store individual events
                for (HeartRateSensorData data : heartRateSensorData) {
                    // 2 = data, 0 = n/a, 1 = heart, 1 = first version
                    FileItem.writeField(fos, BitUtility.getBytes((short)0x2011), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getHeartRate()), BitUtility.getBytes(data.getAccuracy()));
                }

                for (StepCounterSensorData data : stepCounterSensorData) {
                    // 2 = data, 0 = n/a, 2 = steps, 1 = first version
                    FileItem.writeField(fos, BitUtility.getBytes((short)0x2021), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getStepsCount()), BitUtility.getBytes(data.getAccuracy()));
                }

                for (PressureSensorData data : pressureSensorData) {
                    // 2 = data, 0 = n/a, 3 = pressure, 1 = first version
                    FileItem.writeField(fos, BitUtility.getBytes((short)0x2031), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getPressure()), BitUtility.getBytes(data.getAccuracy()));
                }

                for (GeoLocationData data : geoLocationData) {
                    Location location = data.getLocation();

                    // 2 = data, 0 = n/a, 4 = geo, 1 = first version
                    FileItem.writeField(fos,
                            BitUtility.getBytes((short)0x2041),
                            BitUtility.getBytes(data.getTimestamp()),
                            BitUtility.getBytes(location.getElapsedRealtimeNanos()),
                            BitUtility.getBytes(location.getTime()),
                            BitUtility.getBytes(location.getLatitude()),
                            BitUtility.getBytes(location.getLongitude()),
                            BitUtility.getBytes(location.getAccuracy()),
                            BitUtility.getBytes(location.getAltitude()),
                            BitUtility.getBytes(location.getBearing()),
                            BitUtility.getBytes(location.getSpeed()),
                            BitUtility.getBytes(data.getAccuracy()));
                }

                // end of file marker
                FileItem.writeField(fos, BitUtility.getBytes((short)0xffff));
                fos.close();

                succeeded++;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to write data file " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            }
        }

        return succeeded;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (this.finishedCallback != null) {
            this.finishedCallback.accept(integer);
        }
    }
}
