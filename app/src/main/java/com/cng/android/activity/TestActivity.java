package com.cng.android.activity;

import android.app.Activity;
import android.os.Bundle;

import com.cng.android.R;
import com.cng.android.ui.ChartView;
import com.cng.android.util.TestDataProvider;

/**
 * Created by game on 2016/2/22
 */
public class TestActivity extends Activity {
    private ChartView chart;
    private TestDataProvider provider;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        ((ChartView) findViewById (R.id.chart)).setProvider (provider = new TestDataProvider ());
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
        provider.shutdown ();
    }
}