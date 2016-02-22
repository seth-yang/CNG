package com.cng.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.cng.android.util.HandlerDelegate;
import com.cng.android.util.IMessageHandler;

import java.text.DecimalFormat;

/**
 * Created by game on 2016/2/22
 */
public class DashboardView extends View implements IMessageHandler {
    private float radius;
    private float gap = 50;
    private double min = 0, max = 100, range = max - min;
    private Double base = null, value = range / 2;
    private String title;
    private int
            labelColor      = 0xCCCCCCFF,
            pointColor      = Color.YELLOW,
            backgroundColor = Color.BLACK;
    private int trackSize   = 30;

    private Point center;
    private Paint paint;
    private TextPaint textPaint;
    private SweepGradient gradient = new SweepGradient (
            0, 0,
            new int[] {Color.RED, Color.GREEN, Color.GREEN, Color.RED},
            new float[] {0, 5/18f, 5/9f, 5/6f}
    );

    private Handler handler;
    private RectF outer;

    private float normalFontSize = 30, valueFontSize = 40, titleFontSize = 60;
    private float normalStrokeSize = 1f, halfStrokeSize = 2f, valueStrokeSize = 3f;

    private DecimalFormat formatter = new DecimalFormat ("0.0");

    private boolean init = false;

    public DashboardView (Context context) {
        super (context);
        initComponent ();
    }

    public DashboardView (Context context, AttributeSet attrs) {
        super (context, attrs);
        initComponent ();
    }

    public DashboardView (Context context, AttributeSet attrs, int defStyleAttr) {
        super (context, attrs, defStyleAttr);
        initComponent ();
    }

    public float getGap () {
        return gap;
    }

    public DashboardView setGap (float gap) {
        if (this.gap != gap) {
            this.gap = gap;
            repaint ();
        }

        return this;
    }

    public double getMin () {
        return min;
    }

    public DashboardView setMin (double min) {
        if (this.min != min) {
            this.min = min;
            range = max - min;
            repaint ();
        }
        return this;
    }

    public double getMax () {
        return max;
    }

    public DashboardView setMax (double max) {
        if (this.max != max) {
            this.max = max;
            range = max - min;
            repaint ();
        }
        return this;
    }

    public double getBase () {
        return base;
    }

    public DashboardView setBase (Double base) {
        if ((base == null && this.base != null) ||
            (base != null && this.base == null) ||
            (base != null && !base.equals (this.base))) {
            this.base = base;
            repaint ();
        }

        return this;
    }

    public double getValue () {
        return value;
    }

    public DashboardView setValue (double value) {
        if (this.value != value) {
            this.value = value;
            repaint ();
        }
        return this;
    }

    public String getTitle () {
        return title;
    }

    public DashboardView setTitle (String title) {
        String a = String.valueOf (this.title), b = String.valueOf (title);
        if (!a.equals (b)) {
            this.title = title;
            repaint ();
        }

        return this;
    }

    public int getLabelColor () {
        return labelColor;
    }

    public DashboardView setLabelColor (int labelColor) {
        if (this.labelColor != labelColor) {
            this.labelColor = labelColor;
            repaint ();
        }

        return this;
    }

    public int getPointColor () {
        return pointColor;
    }

    public DashboardView setPointColor (int pointColor) {
        if (this.pointColor != pointColor) {
            this.pointColor = pointColor;
            repaint ();
        }

        return this;
    }

    public int getBackgroundColor () {
        return backgroundColor;
    }

    public DashboardView setBoardBackgroundColor (int backgroundColor) {
        if (this.backgroundColor != backgroundColor) {
            this.backgroundColor = backgroundColor;
            repaint ();
        }

        return this;
    }

    public float getNormalFontSize () {
        return normalFontSize;
    }

    public DashboardView setNormalFontSize (float normalFontSize) {
        if (normalFontSize != this.normalFontSize) {
            this.normalFontSize = normalFontSize;
            repaint ();
        }

        return this;
    }

    public float getValueFontSize () {
        return valueFontSize;
    }

    public DashboardView setValueFontSize (float valueFontSize) {
        if (valueFontSize != this.valueFontSize) {
            this.valueFontSize = valueFontSize;
            repaint ();
        }

        return this;
    }

    public float getTitleFontSize () {
        return titleFontSize;
    }

    public DashboardView setTitleFontSize (float titleFontSize) {
        if (titleFontSize != this.titleFontSize) {
            this.titleFontSize = titleFontSize;
            repaint ();
        }

        return this;
    }

    public float getNormalStrokeSize () {
        return normalStrokeSize;
    }

    public DashboardView setNormalStrokeSize (float normalStrokeSize) {
        if (normalStrokeSize != this.normalStrokeSize) {
            this.normalStrokeSize = normalStrokeSize;
            repaint ();
        }

        return this;
    }

    public float getHalfStrokeSize () {
        return halfStrokeSize;
    }

    public DashboardView setHalfStrokeSize (float halfStrokeSize) {
        if (halfStrokeSize != this.halfStrokeSize) {
            this.halfStrokeSize = halfStrokeSize;
            repaint ();
        }

        return this;
    }

    public float getValueStrokeSize () {
        return valueStrokeSize;
    }

    public DashboardView setValueStrokeSize (float valueStrokeSize) {
        if (valueStrokeSize != this.valueStrokeSize) {
            this.valueStrokeSize = valueStrokeSize;
            repaint ();
        }

        return this;
    }

    public DecimalFormat getFormatter () {
        return formatter;
    }

    public DashboardView setFormatter (DecimalFormat formatter) {
        this.formatter = formatter;
        repaint ();

        return this;
    }

    public SweepGradient getGradient () {
        return gradient;
    }

    public DashboardView setGradient (SweepGradient gradient) {
        this.gradient = gradient;
        repaint ();
        return this;
    }

    @Override
    public void handleMessage (Message message) {
        invalidate ();
    }

    @Override
    protected void onDraw (Canvas canvas) {
        if (!init) {
            init ();
        }

        canvas.drawColor (backgroundColor);
        canvas.translate (center.x, center.y);
        // draw track
        canvas.rotate (120);
        paint.setShader (gradient);
        canvas.drawArc (outer, 0, 300, true, paint);
        paint.setShader (null);
        paint.setColor (backgroundColor);
        canvas.drawCircle (0, 0, radius - trackSize, paint);
        canvas.rotate (-120);
        // end of draw track

        textPaint.setColor (labelColor);
        textPaint.setTextSize (normalFontSize);

        paint.setColor (labelColor);
        paint.setStrokeWidth (normalStrokeSize);

        canvas.rotate (-150);
        for (int i = 0; i <= 100; i ++) {
            if (i != 0)
                canvas.rotate (3);

            float y0 = -radius + trackSize, y1 = -radius + 5 + trackSize, width = 1;
            if (i % 5 == 0) {
                if (i % 10 == 0) {
                    double value = i * range / 100 + min;
                    String label = formatter.format (value);
                    float length = textPaint.measureText (label);
                    canvas.drawText (label, -length / 2, -radius - 10, textPaint);
                    y1 += 20;
                    width = halfStrokeSize;
                } else {
                    y1 += 10;
                }
            }
            paint.setStrokeWidth (width);
            canvas.drawLine (0, y0, 0, y1, paint);
        }
        canvas.rotate (-150);

        if (base != null) {
            drawValue (canvas, base);
        }

        if (value == null) value = min;
        drawPointer (canvas, value);

        if (title != null && title.trim ().length () > 0)
            drawLabel (canvas);
        canvas.translate (-center.x, -center.y);
    }

    private void init () {
        int width = getWidth (), height = getHeight ();
        radius = Math.min (width, height) / 2f - gap;
        center = new Point (width / 2, height / 2);

        outer = new RectF (-radius, -radius, radius, radius);

        init = true;
    }

    private void initComponent () {
        paint = new Paint ();
        paint.setAntiAlias (true);

        textPaint = new TextPaint ();
        textPaint.setAntiAlias (true);

        handler = new HandlerDelegate (this);
    }

    private void drawValue (Canvas canvas, double value) {
        float angle = (float) ((value - min) / range * 300) - 150;
        canvas.rotate (angle);

        // draw base label
        textPaint.setColor (labelColor);
        textPaint.setTextSize (valueFontSize);
        String label = formatter.format (value);
        float length = textPaint.measureText (label);
        canvas.drawText (label, -length / 2, -radius - 10, textPaint);

        // draw base rule
        paint.setColor (labelColor);
        paint.setStrokeWidth (valueStrokeSize);
        canvas.drawLine (0, -radius + trackSize + 5, 0, -radius + trackSize + 45, paint);

        canvas.rotate (-angle);
    }

    private void drawPointer (Canvas canvas, double value) {
        float angle = (float) ((value - min) / range * 300) - 150;
        canvas.rotate (angle);

        // draw base label
        textPaint.setColor (pointColor);
        textPaint.setTextSize (valueFontSize);
        String label = formatter.format (value);
        float length = textPaint.measureText (label);
        canvas.drawText (label, -length / 2, -radius - 10, textPaint);

        paint.setColor (pointColor);
        paint.setStrokeWidth (valueStrokeSize);
        canvas.drawLine (0, -radius + trackSize + 5, 0, 20, paint);

        canvas.drawCircle (0, 0, 15, paint);
        paint.setColor (backgroundColor);
        canvas.drawCircle (0, 0, 10, paint);

        canvas.rotate (-angle);
    }

    private void drawLabel (Canvas canvas) {
        textPaint.setTextSize (titleFontSize);
        textPaint.setColor (pointColor);
        float length = textPaint.measureText (title);
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt ();
        float baseline = (radius - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText (title, -length / 2, baseline, textPaint);

    }

    private void repaint () {
        if (Looper.getMainLooper () == Looper.myLooper ()) {
            invalidate ();
        } else {
            handler.sendEmptyMessage (0);
        }
    }
}