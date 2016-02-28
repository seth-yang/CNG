package com.cng.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cng.android.data.EnvData;
import com.cng.android.data.Event;
import com.cng.android.data.EventType;
import com.cng.android.data.ExchangeData;
import com.cng.android.data.SetupItem;
import com.cng.android.util.DataUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cng.android.CNG.D;
/**
 * Created by game on 2016/2/20
 */
public class DBService {
    private static SQLiteDatabase db;

    private static final String TAG = "DBService";

    public static void init (Context context) {
        if (D)
            Log.d (TAG, "trying init database with " + context);

        if (db == null) {
            if (D)
                Log.d (TAG, "The database is not config, init it");

            db = new DBHelper (context).getWritableDatabase ();
            if (D)
                Log.d (TAG, "database create as: " + db);
        } else if (D) {
            Log.d (TAG, "the database has config, nothing to do.");
        }
    }

    public static void dispose () {
        if (db != null) {
            db.close ();
        }
    }

/*
    public static String getSavedBTMac () {
        Cursor cursor = null;
        try {
            String sql =
                    "SELECT " + DBHelper.VALUE +
                    "  FROM " + DBHelper.TABLE_NAME +
                    " WHERE " + DBHelper.NAME + " = '" + SAVED_BT_MAC + "'";
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
        Cursor cursor = query (DBHelper.TABLE_NAME, QUEUE_CAPACITY);
        if (cursor.moveToNext ()) {
            return cursor.getInt (0);
        }

        return -1;
    }

    private static Cursor query (String table, String name) {
        return db.query (
                table,
                new String[] {DBHelper.VALUE},
                DBHelper.NAME + "= ?",
                new String[] {name},
                null, null, null
        );
    }

    private static void insert (String name, String value) {
        String sql =
                "INSERT INTO " + DBHelper.TABLE_NAME +
                " (" + DBHelper.NAME + " , " + DBHelper.VALUE + ") " +
                "VALUES (?, ?)";
        db.execSQL (sql, new String[] {name, value});
    }

    private static void update (String name, String value) {
        String sql = "UPDATE " + DBHelper.TABLE_NAME +
                "   SET " + DBHelper.VALUE + " = ? " +
                " WHERE " + DBHelper.NAME + " = ?";
        db.execSQL (sql, new String[] {value, name});
    }
*/

    public static void execute (InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader (new InputStreamReader (in, "utf-8"));
        String line;
        try {
            db.beginTransaction ();
            while ((line = reader.readLine ()) != null) {
                db.execSQL (line.trim ());
            }
            db.setTransactionSuccessful ();
        } finally {
            db.endTransaction ();
        }
    }

    public static boolean exist (String name) {
        Cursor cursor = null;
        try {
            cursor = db.query (
                    DBSchema.Config.TABLE_NAME,
                    new String[] {"COUNT(*)"},
                    DBSchema.Config.NAME + " = ?",
                    new String[] {name},
                    null,
                    null,
                    null
            );
            if (cursor.moveToNext ()) {
                int count = cursor.getInt (0);
                return count != 0;
            }
            return false;
        } finally {
            if (cursor != null)
                cursor.close ();
        }
    }

    public static List<SetupItem> getSetupItems (boolean all) {
        List<SetupItem> list = new ArrayList<> ();
        Cursor cursor = null;
        try {
            cursor = db.query (
                    DBSchema.Config.TABLE_NAME,
                    DBSchema.Config.ALL_COLUMNS,
                    all ? null : DBSchema.Config.VISIBLE + " = ?",
                    all ? null : new String[] {"true"},
                    null,
                    null,
                    DBSchema.Config.CHINESE
            );
            while (cursor.moveToNext ()) {
                list.add (buildItem (cursor));
            }
            return list;
        } finally {
            if (cursor != null)
                cursor.close ();
        }
    }

    public static void saveSetupItem (SetupItem item) {
        db.execSQL (DBSchema.Config.SQL_INSERT, item.toParameters ());
    }

    public static void saveSetupItems (Collection<SetupItem> items) {
        try {
            db.beginTransaction ();
            for (SetupItem item : items) {
                db.execSQL (DBSchema.Config.SQL_INSERT, item.toParameters ());
            }
            db.setTransactionSuccessful ();
        } finally {
            db.endTransaction ();
        }
    }

    public static void updateSetupItem (String value, String name) {
        db.execSQL (DBSchema.Config.SQL_UPDATE, new String[] {value, name});
    }

    public static SetupItem getSetupItem (String name) {
        Cursor cursor = null;
        try {
            cursor = db.query (
                    DBSchema.Config.TABLE_NAME,
                    DBSchema.Config.ALL_COLUMNS,
                    DBSchema.Config.NAME + " = ?",
                    new String[] {name},
                    null,
                    null,
                    null
            );
            if (cursor.moveToNext ())
                return buildItem (cursor);

            return null;
        } finally {
            if (cursor != null)
                cursor.close ();
        }
    }

    public static void saveData (List<ExchangeData> data) {
        Map<String, Object> map = DataUtil.toMap (data);
        try {
            db.beginTransaction ();
            @SuppressWarnings ("unchecked")
            List<EnvData> dataList = (List<EnvData>) map.get ("D");
            Object[] params = new Object[4];
            for (EnvData e : dataList) {
                params [0] = e.timestamp;
                params [1] = e.temperature;
                params [2] = e.humidity;
                params [3] = e.smoke;
                db.execSQL (DBSchema.SensorData.SQL_INSERT, params);
            }
            db.setTransactionSuccessful ();
        } finally {
            db.endTransaction ();
        }
    }

    public static Map<String, Object> getData () {
        Map<String, Object> map = new HashMap<> ();
        List<EnvData> env_data = new ArrayList<> ();
        Cursor cursor = null;
        try {
            cursor = db.query (
                    DBSchema.SensorData.TABLE_NAME,
                    DBSchema.SensorData.ALL_COLUMNS,
                    null, null, null, null, null
            );
            while (cursor.moveToNext ()) {
                env_data.add (buildSensorData (cursor));
            }
            if (!env_data.isEmpty ()) {
                map.put ("D", env_data);
            }
        } finally {
            if (cursor != null) {
                cursor.close ();
            }
        }

        List<Event> events = new ArrayList<> ();
        try {
            cursor = db.query (
                    DBSchema.Event.TABLE_NAME,
                    DBSchema.Event.ALL_COLUMNS,
                    null, null, null, null, null
            );
            while (cursor.moveToNext ()) {
                events.add (buildEvent (cursor));
            }
            if (!events.isEmpty ())
                map.put ("E", events);
        } finally {
            if (cursor != null)
                cursor.close ();
        }

        return map;
    }

    public static void flushData () {
        db.execSQL (DBSchema.Event.SQL_CLEAR);
        db.execSQL (DBSchema.SensorData.SQL_CLEAR);
    }

/*
    public static void saveData (Collection<Event> data) {

    }

    public static void saveData (Collection<EnvData> data) {
        try {
            db.beginTransaction ();
            Object[] params = new Object[4];
            for (EnvData transformer : data) {
                params [0] = System.currentTimeMillis ();
                params [1] = transformer.temperature;
                params [2] = transformer.humidity;
                db.execSQL (DBSchema.SensorData.SQL_INSERT, params);
            }
            db.setTransactionSuccessful ();
        } finally {
            db.endTransaction ();
        }
    }
*/

    private static SetupItem buildItem (Cursor cursor) {
        SetupItem item = new SetupItem ();
        item.setName (cursor.getString (0));
        item.setChinese (cursor.getString (1));
        String _type = cursor.getString (cursor.getColumnIndex (DBSchema.Config.TYPE));
        SetupItem.Type type = SetupItem.Type.valueOf (_type);
        item.setType (type);
        switch (type) {
            case Boolean:
                boolean b = Boolean.parseBoolean (cursor.getString (2));
                item.setValue (b);
                break;
            case Double:
                item.setValue (cursor.getDouble (2));
                break;
            case Integer:
                item.setValue (cursor.getInt (2));
                break;
            default :
                item.setValue (cursor.getString (2));
                break;
        }
        String editable = cursor.getString (4);
        item.setEditable (Boolean.parseBoolean (editable));
        String visible = cursor.getString (5);
        item.setVisible (Boolean.parseBoolean (visible));
        return item;
    }

    private static EnvData buildSensorData (Cursor cursor) {
        EnvData data = new EnvData ();
        data.timestamp = cursor.getLong (0);
        data.temperature = cursor.getDouble (1);
        data.humidity = cursor.getDouble (2);
        data.smoke = cursor.getDouble (3);
        return data;
    }

    private static Event buildEvent (Cursor cursor) {
        Event event = new Event ();
        event.timestamp = cursor.getLong (0);
        String type_name = cursor.getString (1);
        event.type = EventType.valueOf (type_name);
        event.data = cursor.getString (2);
        return event;
    }
}