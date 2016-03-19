package com.cng.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cng.android.data.EnvData;
import com.cng.android.data.EventTarget;
import com.cng.android.data.ExchangeData;
import com.cng.android.util.DataUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static void execute (InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader (new InputStreamReader (in, "utf-8"));
        String line;
        try {
            db.beginTransaction ();
            while ((line = reader.readLine ()) != null) {
                line = line.trim ();
                if (line.length () == 0) continue;
                if (line.startsWith ("--") || line.startsWith ("#")) continue;
                db.execSQL (line);
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

        List<com.cng.android.data.Event > events = new ArrayList<> ();
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

    public static final class SetupItem {
        public static List<com.cng.android.data.SetupItem> getItems (boolean all) {
            List<com.cng.android.data.SetupItem> list = new ArrayList<> ();
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

        private static com.cng.android.data.SetupItem buildItem (Cursor cursor) {
            com.cng.android.data.SetupItem item = new com.cng.android.data.SetupItem ();
            item.setName (cursor.getString (0));
            item.setChinese (cursor.getString (1));
            String _type = cursor.getString (cursor.getColumnIndex (DBSchema.Config.TYPE));
            com.cng.android.data.SetupItem.Type type = com.cng.android.data.SetupItem.Type.valueOf (_type);
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

        public static void saveItem (com.cng.android.data.SetupItem item) {
            db.execSQL (DBSchema.Config.SQL_INSERT, item.toParameters ());
        }

        public static void saveItems (Collection<com.cng.android.data.SetupItem> items) {
            try {
                db.beginTransaction ();
                for (com.cng.android.data.SetupItem item : items) {
                    db.execSQL (DBSchema.Config.SQL_INSERT, item.toParameters ());
                }
                db.setTransactionSuccessful ();
            } finally {
                db.endTransaction ();
            }
        }

        public static void updateItem (String value, String name) {
            db.execSQL (DBSchema.Config.SQL_UPDATE, new String[] {value, name});
        }

        public static com.cng.android.data.SetupItem getItem (String name) {
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

        public static int getIntValue (String name, int defaultValue) {
            Cursor cursor = null;
            try {
                String sql =
                        "SELECT " + DBSchema.Config.VALUE +
                                "  FROM " + DBSchema.Config.TABLE_NAME +
                                " WHERE " + DBSchema.Config.NAME + " = ?";
                cursor = db.rawQuery (sql, new String[]{name});
                if (cursor.moveToNext ()) {
                    return cursor.getInt (0);
                }
                return defaultValue;
            } finally {
                if (cursor != null) {
                    cursor.close ();
                }
            }
        }

        public static String getStringValue (String name) {
            Cursor cursor = null;
            try {
                String sql =
                        "SELECT " + DBSchema.Config.VALUE +
                                "  FROM " + DBSchema.Config.TABLE_NAME +
                                " WHERE " + DBSchema.Config.NAME + " = ?";
                cursor = db.rawQuery (sql, new String[] {name});
                if (cursor.moveToNext ()) {
                    return cursor.getString (0);
                }
                return null;
            } finally {
                if (cursor != null) {
                    cursor.close ();
                }
            }
        }
    }

    public static final class Card {
        public static boolean isCardValid (int code) {
            Cursor cursor = null;
            try {
                String sql =
                        "SELECT COUNT(*) " +
                        "  FROM " + DBSchema.Card.TABLE_NAME +
                        " WHERE " + DBSchema.Card.CARD_NO + " = ?";
                cursor = db.rawQuery (sql, new String[] {String.valueOf (code)});
                return cursor.moveToNext () && cursor.getInt (0) > 0;
            } finally {
                if (cursor != null) cursor.close ();
            }
        }

        public static void save (Integer... codes) {
            save (Arrays.asList (codes));
        }

        public static void save (Collection<Integer> codes) {
            db.beginTransaction ();
            try {
                for (Integer code : codes) {
                    if (!isCardValid (code)) {
                        db.execSQL (DBSchema.Card.SQL_INSERT, new Object[] {code});
                    }
                }
                db.setTransactionSuccessful ();
            } finally {
                db.endTransaction ();
            }
        }
    }

    public static final class Event {
        public static void save (com.cng.android.data.Event event) {
            db.execSQL (
                    DBSchema.Event.SQL_INSERT,
                    new Object[] {event.timestamp, event.type.name (), event.data}
            );
        }
    }

    public static final class IRCode {
        public static List<com.cng.android.data.IRCode> getIrCodes () {
            Cursor cursor = null;
            try {
                cursor = db.query (DBSchema.IR_Code.TABLE_NAME, DBSchema.IR_Code.ALL_COLUMNS, null, null, null, null, DBSchema.IR_Code.CHINESE);
                List<com.cng.android.data.IRCode> list = new ArrayList<> ();
                while (cursor.moveToNext ())
                    list.add (buildIrCode (cursor));
                return list;
            } finally {
                if (cursor != null)
                    cursor.close ();
            }
        }

        public static void update (String name, int code) {
            db.execSQL (DBSchema.IR_Code.SQL_UPDATE, new Object[] {code, name});
        }

        private static com.cng.android.data.IRCode buildIrCode (Cursor cursor) {
            com.cng.android.data.IRCode code = new com.cng.android.data.IRCode ();
            code.name    = cursor.getString (0);
            code.chinese = cursor.getString (1);
            String temp  = cursor.getString (2);
            if (temp != null && temp.trim ().length () > 0) try {
                code.code = Integer.parseInt (temp);
            } catch (Exception ex) {
                // ignore
            }

            return code;
        }
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



    private static EnvData buildSensorData (Cursor cursor) {
        EnvData data = new EnvData ();
        data.timestamp = cursor.getLong (0);
        data.temperature = cursor.getDouble (1);
        data.humidity = cursor.getDouble (2);
        data.smoke = cursor.getDouble (3);
        return data;
    }

    private static com.cng.android.data.Event buildEvent (Cursor cursor) {
        com.cng.android.data.Event event = new com.cng.android.data.Event ();
        event.timestamp = cursor.getLong (0);
        String type_name = cursor.getString (1);
        event.type = EventTarget.valueOf (type_name);
        event.data = cursor.getString (2);
        return event;
    }
}