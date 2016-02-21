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

    public static final String TABLE_CONF = "conf", F_NAME = "_name", F_VALUE = "_value";
    private static final String DDL_CREATE = "CREATE TABLE " + TABLE_CONF +
            " (" + F_NAME + " VARCHAR(32) NOT NULL PRIMARY KEY, " +
            F_VALUE + " TEXT)";

    public DBHelper (Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        if (CNG.D)
            Log.d (SQLiteOpenHelper.class.getSimpleName (), "Creating Data Table: " + DDL_CREATE);
        db.execSQL (DDL_CREATE);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
