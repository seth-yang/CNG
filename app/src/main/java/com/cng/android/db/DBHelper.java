package com.cng.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cng.android.CNG;

/**
 * Created by game on 2016/2/20
 */
class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME    = "CNG.db";
    public static final int    DATABASE_VERSION = 1;

    public DBHelper (Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        if (CNG.D)
            Log.d (SQLiteOpenHelper.class.getSimpleName (), "preparing to create table " + DBSchema.Config.TABLE_NAME);
        db.execSQL (DBSchema.Config.DDL_CREATE);
        db.execSQL (DBSchema.SensorData.DDL_CREATE);
        db.execSQL (DBSchema.Event.DDL_CREATE);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
