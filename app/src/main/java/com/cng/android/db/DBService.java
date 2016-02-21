package com.cng.android.db;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by game on 2016/2/20
 */
public class DBService {
    private static SQLiteDatabase db;

    private static final String SAVED_BT_MAC = "saved_bt_mac";

/*
    private static final int GET_SAVED_BT_MAC = 1,
                             SAVE_OR_UPDATE_BT_MAC = 2;
*/

    public static void init (Application application) {
        db = new DBHelper (application).getWritableDatabase ();
    }

    public static void dispose () {
        if (db != null) {
            db.close ();
        }
    }

    public static String getSavedBTMac () {
        Cursor cursor = null;
        try {
            String sql =
                    "SELECT " + DBHelper.F_VALUE +
                    "  FROM " + DBHelper.TABLE_CONF +
                    " WHERE " + DBHelper.F_NAME + " = '" + SAVED_BT_MAC + "'";
            cursor = db.rawQuery (sql, null);

            String mac = null;
            if (cursor.moveToNext ()) {
                mac = cursor.getString (0);
            }
            return mac;
        } finally {
            if (cursor != null) cursor.close ();
        }
    }

    public static void saveOrUpdateBTMac (String mac) {
        String saved = getSavedBTMac ();
        if (saved == null) {
            String sql =
                    "INSERT INTO " + DBHelper.TABLE_CONF +
                    " (" + DBHelper.F_NAME + " , " + DBHelper.F_VALUE + ") " +
                    "VALUES (?, ?)";
            db.execSQL (sql, new String[]{SAVED_BT_MAC, mac});
        } else {
            String sql = "UPDATE " + DBHelper.TABLE_CONF +
                         "   SET " + DBHelper.F_VALUE + " = ? " +
                         " WHERE " + DBHelper.F_NAME + " = ?";
            db.execSQL (sql, new String[] {mac, SAVED_BT_MAC});
        }
    }

/*
    private static void handleMessage (Message message) {
        Bundle bundle = message.getData ();
        switch (message.what) {
            case GET_SAVED_BT_MAC :
                break;
            case SAVE_OR_UPDATE_BT_MAC :
                String mac = bundle.getString ("mac");
                saveOrUpdateBTMac (mac);
                break;
        }
    }
*/
}