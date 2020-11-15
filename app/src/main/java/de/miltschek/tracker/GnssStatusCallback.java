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

import android.location.GnssStatus;
import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;

/**
 * Listener of GNSS status events.
 */
public class GnssStatusCallback extends GnssStatus.Callback {
    private static final String TAG = GnssStatusCallback.class.getSimpleName();

    /** Maximum age of a location event to treat it as valid. */
    private static final long MAX_STATUS_AGE_MS = 10L * 1000;

    private long numSatellitesTimestamp;
    private int[] numSatellitesUsed = new int[8];

    public int getNumSatellitesUsed(int constellation) {
        return numSatellitesUsed[constellation];
    }

    /**
     * Gets the amount of satellites of the constellation that has the most satellites
     * being received.
     * @return the amount of tracked satellites of the best constellation at the moment
     */
    public int getBestSatellitesCount() {
        int max = 0;

        if (numSatellitesTimestamp >= SystemClock.elapsedRealtime() - MAX_STATUS_AGE_MS) {
            for (int num : numSatellitesUsed) {
                if (num > max)
                    max = num;
            }
        }

        return max;
    }

    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {
        numSatellitesTimestamp = SystemClock.elapsedRealtime();
        int[] localNumSatellitesUsed = new int[numSatellitesUsed.length];

        for (int n = status.getSatelliteCount() - 1; n >= 0; n--) {
            if (status.usedInFix(n)) {
                int constellation = status.getConstellationType(n);
                if (constellation < localNumSatellitesUsed.length && constellation >= 0) {
                    localNumSatellitesUsed[constellation]++;
                }
            }
        }

        for (int n = localNumSatellitesUsed.length - 1; n >= 0; n--) {
            numSatellitesUsed[n] = localNumSatellitesUsed[n];
        }
        Log.d(TAG, "Num satellites used " + Arrays.toString(localNumSatellitesUsed));
    }
}
