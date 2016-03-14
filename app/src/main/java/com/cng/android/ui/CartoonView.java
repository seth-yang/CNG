package com.cng.android.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.cng.android.R;

/**
 * Created by game on 2016/3/14
 */
public class CartoonView extends View {
    private Bitmap[] pics = null;
    private int index   = 0, width, height;
    private int countdown = 0;
    private Point point = new Point ();
    private Paint paint = new Paint ();
    private int color   = 0x383838;
    private BlinkMode mode = BlinkMode.Normal;

    public static final int MODE_NORMAL     = 0;
    public static final int MODE_MATCHED    = 1;
    public static final int MODE_MISMATCHED = 2;

    public enum BlinkMode {
        Normal, Matched, Mismatched
    }

    public CartoonView (Context context) {
        super (context);
        init ();
    }

    public CartoonView (Context context, AttributeSet attrs) {
        super (context, attrs);
        init ();
    }

    public CartoonView (Context context, AttributeSet attrs, int defStyleAttr) {
        super (context, attrs, defStyleAttr);
        init ();
    }

    public CartoonView (Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super (context, attrs, defStyleAttr, defStyleRes);
        init ();
    }

    @Override
    public int getMinimumHeight () {
        return pics == null ? super.getMinimumHeight () : pics [0].getHeight ();
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode  = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize  = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int pic_width = pics [0].getWidth (), pic_height = pics [0].getHeight ();
        int min_width  = getPaddingEnd () + getPaddingStart () + pic_width,
            min_height = getPaddingTop () + getPaddingBottom () + pic_height;
        switch (widthMode) {
            case MeasureSpec.AT_MOST :
                width = Math.min (widthSize, min_width);
                break;
            case MeasureSpec.EXACTLY :
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = min_width;
                break;
        }
        switch (heightMode) {
            case MeasureSpec.AT_MOST :
                height = Math.min (heightSize, min_height);
                break;
            case MeasureSpec.EXACTLY :
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED :
                height = min_height;
                break;
        }
        point.x = (width - pic_width) / 2;
        point.y = (height - pic_height) / 2;
        setMeasuredDimension (width, height);
    }

    @Override
    public void setBackgroundColor (int color) {
        super.setBackgroundColor (color);
        this.color = color;
    }

    @Override
    public void setBackground (Drawable background) {
        super.setBackground (background);
        if (background instanceof ColorDrawable) {
            this.color = ((ColorDrawable) background).getColor ();
        }
    }

    public void blink (BlinkMode mode) {
        if (mode != BlinkMode.Normal) {
            this.mode = mode;
            countdown = 6;
            index = 0;
        }
    }

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw (canvas);
        canvas.drawColor (color);
        Bitmap pic;
        if (mode == BlinkMode.Normal) {
            pic = pics[index];
            index++;
            if (index >= 6) index = 0;
        } else {
            int target;
            if (mode == BlinkMode.Matched) {
                target = 6;
            } else {
                target = 7;
            }
            if (countdown > 0) {
                pic = pics[index];
                if (index == 0)
                    index = target;
                else
                    index = 0;
                countdown--;
            } else {
                pic = pics[0];
                index = 0;
                mode = BlinkMode.Normal;
            }
        }
        if (pic != null) {
            canvas.drawBitmap (pic, point.x, point.y, paint);
        }
        postInvalidateDelayed (150, 148, 0, 282, height);
    }

    private void init () {
        int[] resources = {
                R.drawable.infrared_sensor_0,
                R.drawable.infrared_sensor_1,
                R.drawable.infrared_sensor_2,
                R.drawable.infrared_sensor_3,
                R.drawable.infrared_sensor_4,
                R.drawable.infrared_sensor_5
        };
        pics = new Bitmap[8];
        Resources res = getResources ();
        for (int i = 0; i < resources.length; i ++) {
            int id = resources [i];
            BitmapDrawable bm = (BitmapDrawable) res.getDrawable (id);
            if (bm != null) {
                Bitmap src = bm.getBitmap ();
                pics [i] = src;
            }
        }
        BitmapDrawable bd = (BitmapDrawable) res.getDrawable (R.drawable.infrared_sensor_matched);
        if (bd != null) {
            pics [6] = bd.getBitmap ();
        }
        bd = (BitmapDrawable) res.getDrawable (R.drawable.infrared_sensor_mismatched);
        if (bd != null)
            pics [7] = bd.getBitmap ();
    }
}
