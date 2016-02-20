package com.cng.android;

import android.app.Application;

import com.cng.android.db.DBService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by game on 2016/2/20
 */
public class CNG extends Application {
    private static final ExecutorService service = Executors.newCachedThreadPool ();
    private static CNG instance;

    public static void runInNonUIThread (Runnable runnable) {
        service.execute (runnable);
    }

//    public static void runInNonUIThread ()

    @Override
    public void onCreate () {
        super.onCreate ();
        instance = this;
        runInNonUIThread (new Runnable () {
            @Override
            public void run () {
                DBService.init (instance);
            }
        });
    }
}