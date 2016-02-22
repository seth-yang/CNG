package com.cng.android.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.SweepGradient;
import android.os.Bundle;

import com.cng.android.R;
import com.cng.android.ui.DashboardView;

import java.text.DecimalFormat;

/**
 * Created by game on 2016/2/22
 */
public class TestActivity extends Activity {
    private DashboardView temperature, humidity;
    private Thread thread;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_test);
        temperature = (DashboardView) findViewById (R.id.temperature);
        temperature.setTitle (getString (R.string.title_temperature))
                .setMin (-25).setMax (125);

        humidity = (DashboardView) findViewById (R.id.humidity);
        humidity.setTitle (getString (R.string.title_humidity))
                .setFormatter (new DecimalFormat ("0"));
        thread = new Thread () {
            @Override
            public void run () {
                while (true) {
                    double value = Math.random () * 150 - 25;
                    temperature.setValue (value);

                    value = Math.random () * 100;
                    humidity.setValue (value);
                    try {
                        sleep (1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }
            }
        };
        thread.start ();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
        thread.interrupt ();
    }
}