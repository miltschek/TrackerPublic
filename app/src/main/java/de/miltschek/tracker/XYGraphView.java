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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Two-dimensional graph.
 */
public class XYGraphView extends View {

    private Paint paint, greenPaint, redPaint;
    private int w, h;
    private List<XYData> xyDataCollection = new ArrayList<>();

    /**
     * Creates the two-dimensional graph.
     * @param context application context.
     * @param attrs attributes for creation of the view.
     */
    public XYGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenPaint.setStyle(Paint.Style.FILL);
        greenPaint.setColor(Color.GREEN);

        redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redPaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.RED);

        //paint.setTextSize(textHeight);
        setMinimumHeight(70);
    }

    /**
     * Creates a new data set that is to be displayed with the specified color
     * and of the specified maximum value (used for proportional scaling).
     * @param color color value.
     * @param maxValue maximal value for the y-axis.
     * @return descriptor of the data set.
     */
    public XYData addDataSet(int color, float maxValue) {
        XYData xyData = new XYData(200, color, maxValue);

        xyDataCollection.add(xyData);
        return xyData;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int scale = 6;
        final int xSpace = w / scale;

        for (XYData xyData : xyDataCollection) {
            int size = xyData.size();

            for (int n = 0; n < xyData.markers.length; n++) {
                float height = h - xyData.markers[n] * h;
                canvas.drawLine(0, height, w, height, xyData.markerPaints[n]);
            }

            if (size > 0) {
                Paint xyPaint = xyData.paint;

                // starting element to be drawn
                int offset = (xSpace >= size ? 0 : size - xSpace);
                // starting position on the graph to be drawn
                int begin = (xSpace >= size ? xSpace - size : 0);

                // guaranteed: offset <= size
                // so 'n' will not pass the stopIndex
                int n = xyData.startIndex + offset;
                if (n >= xyData.valuesNormalized.length) {
                    n -= xyData.valuesNormalized.length;
                }

                int xPosition = begin;

                while (true) {
                    float currentY = h - (xyData.valuesNormalized[n] * h);
                    canvas.drawRect(xPosition * scale, currentY, (xPosition + 1) * scale - 1, h, xyPaint);

                    if (n == xyData.stopIndex) {
                        break;
                    }

                    xPosition++;
                    n++;
                    if (n >= xyData.valuesNormalized.length) {
                        n = 0;
                    }
                }

                String header = String.valueOf(xyData.lastValue);
                Rect boundsHeader = new Rect(), boundsSubtitle = new Rect();

                String subtitle = String.format("Ø %.0f", xyData.avgValue);
                xyData.paintSubtitle.getTextBounds(subtitle, 0, subtitle.length(), boundsSubtitle);
                canvas.drawText(subtitle, 2, /*boundsHeader.height() - */-boundsSubtitle.top + 5, xyData.paintSubtitleBlend);
                canvas.drawText(subtitle, 0, /*boundsHeader.height() - */-boundsSubtitle.top + 3, xyData.paintSubtitle);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        for (XYData xyData : xyDataCollection) {
            xyData.paintHeader.setTextSize(h / 2);
            xyData.paintSubtitle.setTextSize(h / 4);
            xyData.paintHeaderBlend.setTextSize(h / 2);
            xyData.paintSubtitleBlend.setTextSize(h / 4);
        }
    }

    /**
     * Descriptor of a data set.
     */
    public class XYData {
        private int startIndex, stopIndex;
        private float maxValue;
        private float[] valuesNormalized;
        private float avgValue;
        private int avgCount;
        private int lastValue;
        private float[] markers = new float[0];

        private final Paint paint, paintHeader, paintSubtitle, paintHeaderBlend, paintSubtitleBlend;
        private Paint[] markerPaints = new Paint[0];

        /**
         * Creates a descriptor.
         * @param size buffer size for historical data.
         * @param color color of the data set (32bit argb value).
         * @param maxValue maximum value.
         */
        public XYData(int size, int color, float maxValue) {
            this.valuesNormalized = new float[size];
            this.paint = new Paint();
            this.paint.setColor(color);
            this.paint.setStyle(Paint.Style.FILL_AND_STROKE);

            this.paintHeader = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.paintHeader.setColor(ColorUtils.blendARGB(color, Color.BLACK, 0.2f));
            this.paintHeader.setStyle(Paint.Style.FILL_AND_STROKE);
            this.paintHeader.setElegantTextHeight(true);
            this.paintHeader.setTypeface(Typeface.DEFAULT_BOLD);

            this.paintSubtitle = new Paint(this.paintHeader);

            this.paintHeaderBlend = new Paint(this.paintHeader);
            this.paintHeaderBlend.setColor(ColorUtils.blendARGB(this.paintHeader.getColor(), Color.BLACK, 0.8f));

            this.paintSubtitleBlend = new Paint(this.paintSubtitle);
            this.paintSubtitleBlend.setColor(ColorUtils.blendARGB(this.paintSubtitle.getColor(), Color.BLACK, 0.8f));

            this.maxValue = maxValue;
        }

        /**
         * Creates a new horizontal marker on a specified value level.
         * @param value value to be marked across the whole graph.
         * @param color color to be used for the marker (32bit argb value).
         */
        public void addMarker(int value, int color) {
            float[] oldMarkers = markers;
            markers = new float[markers.length + 1];
            markers[markers.length - 1] = value / maxValue;

            Paint[] oldMarkerPaints = markerPaints;
            markerPaints = new Paint[markerPaints.length + 1];
            Paint markerPaint = new Paint();
            markerPaint.setColor(color);
            markerPaint.setStyle(Paint.Style.FILL);
            markerPaints[markerPaints.length - 1] = markerPaint;

            for (int n = 0; n < oldMarkers.length; n++) {
                markers[n] = oldMarkers[n];
                markerPaints[n] = oldMarkerPaints[n];
            }
        }

        /**
         * Clears all stored data.
         */
        public void clear() {
            startIndex = 0;
            stopIndex = 0;
            avgCount = 0;
            avgValue = 0;
            lastValue = 0;
            XYGraphView.this.invalidate();
        }

        /**
         * Gets the amount of stored values.
         * @return amount of stored values.
         */
        public int size() {
            return stopIndex - startIndex + ((stopIndex < startIndex) ? valuesNormalized.length : 0);
        }

        /**
         * Puts a new value on top of the history buffer.
         * @param value value to be stored.
         */
        public void put(int value) {
            stopIndex++;
            // empty vector is a special case: stopIndex becomes 0
            // no shifting in such case
            if (stopIndex > 0) {
                // roll the stop index
                if (stopIndex >= valuesNormalized.length) {
                    stopIndex = 0;
                }

                // push the start index if the capacity reached
                if (stopIndex == startIndex) {
                    startIndex++;
                    // roll the start index
                    if (startIndex >= valuesNormalized.length) {
                        startIndex = 0;
                    }
                }
            }

            valuesNormalized[stopIndex] = value / maxValue;
            lastValue = value;
            avgValue = (float)avgCount++ / avgCount * avgValue + (float)value / avgCount;
            XYGraphView.this.invalidate();
        }

        /**
         * Gets an average value out of all stored historical values.
         * @return an average value out of all stored historical values.
         */
        public float getAvgValue() {
            return avgValue;
        }

        /**
         * Gets the latest value that has been added.
         * @return the latest value that has been added.
         */
        public int getLastValue() {
            return lastValue;
        }
    }
}
