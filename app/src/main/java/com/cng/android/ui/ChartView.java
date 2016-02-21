package com.cng.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.CursorAnchorInfo;

import com.cng.android.data.IDataProvider;
import com.cng.android.data.Transformer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class ChartView extends View {
    private long interval = 1000;
    private Paint LINE_PAIN = new Paint ();
    private TextPaint textPaint = new TextPaint ();
    private static final int LEFT = 100, BOTTOM = 100;
    private IDataProvider provider;

    public ChartView (Context context) {
        super (context);
        init ();
    }

    public ChartView (Context context, AttributeSet attrs) {
        super (context, attrs);
        init ();
    }

    public ChartView (Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        init ();
    }

    private void init () {
        LINE_PAIN.setColor (0xFFFFFF);
        textPaint.setTextSize (25);
    }

    @Override
    protected void onDraw (Canvas canvas) {
        if (provider == null)
            return;
        drawBackground (canvas);
        drawT (canvas);
        postInvalidateDelayed (interval);
    }

    public void setInterval (long interval) {
        this.interval = interval;
    }

    public void setProvider (IDataProvider provider) {
        this.provider = provider;
    }

    private Holder processData () {
//        Transformer[] nodes = (Transformer[]) provider.getNodes ().toArray ();
        @SuppressWarnings ("unchecked")
        List<Transformer> nodes = (List<Transformer>) provider.getNodes ();
        holder.min = nodes.get (0).timestamp;
        holder.max = nodes.get (nodes.size () - 1).timestamp;
        for (Transformer t : nodes) {
            if (t.humidity > holder.maxH) holder.maxH = t.humidity;
            if (t.humidity < holder.minH) holder.minH = t.humidity;
            if (t.temperature > holder.maxT) holder.maxT = t.temperature;
            if (t.temperature < holder.minT) holder.minT = t.temperature;
        }

        return holder;
//        return new Holder (min, max, minH, maxH, minT, maxT);
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat ("HH:mm:ss");
    private static final DecimalFormat df = new DecimalFormat ("##0.00");

    private void drawBackground (Canvas canvas) {
        int width = getWidth (), height = getHeight ();
        int y = height - BOTTOM;
        LINE_PAIN.setColor (0x66FFFFFF);
        canvas.drawLine (LEFT, 0, LEFT, y, LINE_PAIN);
        canvas.drawLine (LEFT, y, width, y, LINE_PAIN);
        holder = processData ();

        textPaint.setColor (0xFFFFFF00);
        String label = sdf.format (holder.min);
        drawLabel (canvas, LEFT, y, label, 45);
        label = sdf.format (holder.max);
        drawLabel (canvas, width, y, label, 45);

        textPaint.setColor (0xFF00FF00);
        label = df.format (holder.minT);
        drawLabel (canvas, LEFT, y, label, 0);
        label = df.format (holder.maxT);
        drawLabel (canvas, LEFT, 30, label, 0);
    }

    static final int GAP = 10;
    Holder holder = new Holder ();

    private void drawLabel (Canvas canvas, int x, int y, String label, float angle) {
        float length = textPaint.measureText (label);
        canvas.translate (x - GAP, y - GAP);
        if (angle != 0)
            canvas.rotate (-angle);
        canvas.drawText (label, -length, 0, textPaint);
        if (angle != 0)
            canvas.rotate (angle);
        canvas.translate (-x + GAP, -y + GAP);
    }

    private void drawT (Canvas canvas) {
        double delta = holder.maxT - holder.minT;
        double base  = getHeight () - BOTTOM;
        @SuppressWarnings ("unchecked")
        List<Transformer> nodes = (List<Transformer>) provider.getNodes ();
        List<Point> points = new ArrayList<> ();
        int x = LEFT;
        Path path = new Path ();
        LINE_PAIN.setColor (0xFF00FF00);
        int index = 0;
        for (Transformer t : nodes) {
            double d = t.temperature - holder.minT;
            float y = (float) (d / delta);
            y = (float) (base * (1 - y));
            canvas.drawCircle (x, y, 2, LINE_PAIN);
            points.add (new Point (x, (int) y));
            x += 20;
        }
        for (int i = 0; i < points.size () - 1; i ++) {
            Point p1 = points.get (i), p2 = points.get (i + 1);
            canvas.drawLine (p1.x, p1.y, p2.x, p2.y, LINE_PAIN);
        }
//        canvas.drawPath (path, LINE_PAIN);
    }

    private static class Holder {
        double min, max, minH = 1000, maxH = -1, minT = 1000, maxT = -25;

        public Holder () {}

        public Holder (double min, double max, double minH, double maxH, double minT, double maxT) {
            this.min = min;
            this.max = max;
            this.minH = minH;
            this.maxH = maxH;
            this.minT = minT;
            this.maxT = maxT;
        }
    }
}