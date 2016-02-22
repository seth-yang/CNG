package com.cng.android.activity;

import android.app.Activity;
import android.os.Bundle;

import com.cng.android.R;
import com.cng.android.ui.DashboardView;
import com.cng.android.util.TestDataProvider;

/**
 * Created by game on 2016/2/22
 */
public class TestActivity extends Activity {
    private DashboardView dashboard;
    private Thread thread;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_test);
        dashboard = (DashboardView) findViewById (R.id.dashboard);
        dashboard.setTitle (getString (R.string.title_temperature));

        thread = new Thread () {
            @Override
            public void run () {
                while (true) {
                    double value = Math.random () * 150 - 25;
                    dashboard.setValue (value);

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