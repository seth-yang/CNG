package com.cng.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static com.cng.android.CNG.D;
/**
 * Created by game on 2016/2/20
 */
public class DBService {
    private static SQLiteDatabase db;

    private static final String SAVED_BT_MAC = "saved.bt.mac";
    private static final String QUEUE_CAPACITY = "queue.capacity";
    private static final String TAG = "DBService";

/*
    private static final int GET_SAVED_BT_MAC = 1,
                             SAVE_OR_UPDATE_BT_MAC = 2;
*/

    public static void init (Context context) {
        if (D)
            Log.d (TAG, "trying init database with " + context);

        if (db == null) {
            if (D)
                Log.d (TAG, "The database is not config, init it");

            db = new DBHelper (context).getWritableDatabase ();
            if (D)
                Log.d (TAG, "database create as: " + db);

            if (D)
                Log.d (TAG, "trying to fetch queue capacity ");
            if (getQueueCapacity () < 0) {
                if (D)
                    Log.d (TAG, "queue capacity is not set, set it to 3600");
                insert (QUEUE_CAPACITY, String.valueOf (3600));
                if (D)
                    Log.d (TAG, "capacity set!");
            }
        } else if (D) {
            Log.d (TAG, "the database has config, nothing to do.");
        }
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
            insert (SAVED_BT_MAC, mac);
        } else {
            update (mac, SAVED_BT_MAC);
        }
    }

    public static int getQueueCapacity () {
        Cursor cursor = query (DBHelper.TABLE_CONF, QUEUE_CAPACITY);
        if (cursor.moveToNext ()) {
            return cursor.getInt (0);
        }

        return -1;
    }

    private static Cursor query (String table, String name) {
        return db.query (
                table,
                new String[] {DBHelper.F_VALUE},
                DBHelper.F_NAME + "= ?",
                new String[] {name},
                null, null, null
        );
    }

    private static void insert (String name, String value) {
        String sql =
                "INSERT INTO " + DBHelper.TABLE_CONF +
                " (" + DBHelper.F_NAME + " , " + DBHelper.F_VALUE + ") " +
                "VALUES (?, ?)";
        db.execSQL (sql, new String[] {name, value});
    }

    private static void update (String name, String value) {
        String sql = "UPDATE " + DBHelper.TABLE_CONF +
                "   SET " + DBHelper.F_VALUE + " = ? " +
                " WHERE " + DBHelper.F_NAME + " = ?";
        db.execSQL (sql, new String[] {value, name});
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