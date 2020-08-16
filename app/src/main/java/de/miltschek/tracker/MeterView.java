package de.miltschek.tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class MeterView extends View {
    private int width, height;
    private float outRadius;
    private float inRadius;
    private float midX;
    private float midY;
    private Paint bgPaint;
    private int totalSegments;
    private Paint[] segmentPaint;
    private float stepDeg;
    private float segmentSizeDeg;
    private float value = 1f;

    public MeterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        bgPaint.setColor(Color.TRANSPARENT);

        setSegments(40);
    }

    public final void setSegments(int totalSegments) {
        this.totalSegments = totalSegments;

        this.stepDeg = 360f / totalSegments;
        this.segmentSizeDeg = 0.7f * stepDeg;

        calculateSegmentPaint();

        invalidate();
    }

    public void setValue(float value) {
        this.value = value;
        invalidate();
    }

    private void calculateSegmentPaint() {
        segmentPaint = new Paint[totalSegments];
        for (int n = 0; n < segmentPaint.length; n++) {
            float proportion = (float)n / totalSegments;

            segmentPaint[n] = new Paint(Paint.ANTI_ALIAS_FLAG);
            segmentPaint[n].setStyle(Paint.Style.FILL);

            float rFactor = (proportion < 0.375f) ? (0.375f - proportion) / 0.375f :
                    (proportion < 0.5f) ? 0 :
                            (proportion < 0.675f) ? (proportion - 0.5f) / 0.175f :
                                    (proportion < 0.875f) ? 1 :
                                            1.875f - proportion;
            float gFactor = (proportion < 0.75f) ? 1 :
                    (proportion < 0.875f) ? (0.875f - proportion) / 0.125f :
                            0;
            float bFactor = (proportion < 0.375f) ? (0.375f - proportion) / 0.375f :
                    0;

            segmentPaint[n].setColor((0xff << 24) + ((int)(rFactor * 0xff) << 16) + ((int)(gFactor * 0xff) << 8) + (int)(bFactor * 0xff));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*Path path = new Path();
        path.addCircle((float)midX, (float)midY, (float)outRadius, Path.Direction.CW);
        canvas.clipPath(path);
        path = new Path();
        path.addCircle((float)midX, (float)midY, (float)inRadius, Path.Direction.CW);
        canvas.clipPath(path, Region.Op.DIFFERENCE);*/

        canvas.drawRect(0, 0, width, height, bgPaint);

        int stop = (int)(value * totalSegments);
        if (totalSegments < stop) {
            stop = totalSegments;
        }

        for (int n = 0; n < stop; n++) {
            float posDeg = n * stepDeg - 90;
            canvas.drawArc(2, 2, width - 2, height - 2, posDeg - segmentSizeDeg / 2f, segmentSizeDeg, true, segmentPaint[n]);
        }

        canvas.drawCircle(midX, midY, inRadius, bgPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        this.outRadius = ((width < height) ? width : height) / 2f;
        this.inRadius = outRadius - 10f;
        this.midX = width / 2f;
        this.midY = height / 2f;
    }
}
