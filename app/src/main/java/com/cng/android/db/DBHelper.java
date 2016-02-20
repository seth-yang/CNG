package com.cng.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by game on 2016/2/20
 */
class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME    = "CNG.db";
    public static final int    DATABASE_VERSION = 1;

    public static final String TABLE_CONF = "conf";
    private static final String DDL_CREATE = "CREATE TABLE conf (name VARCHAR(32) NOT NULL PRIMARY KEY, value TEXT)";

    public DBHelper (Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        db.execSQL (DDL_CREATE);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
