package com.cng.android.db;

/**
 * Created by seth.yang on 2016/2/23
 */
public interface DBSchema {
    class Config {
        public static final String TABLE_NAME = "conf";
        public static final String
                NAME = "_name", VALUE = "_value", CHINESE = "_chinese",
                TYPE = "_type", EDITABLE = "_editable", VISIBLE = "_visible";
        public static final String[] ALL_COLUMNS = {
                NAME, CHINESE, VALUE, TYPE, EDITABLE, VISIBLE
        };
        public static final String DDL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " +
                NAME + " VARCHAR(64) NOT NULL PRIMARY KEY, " +
                CHINESE  + " VARCHAR(256) NOT NULL, " +
                VALUE + " TEXT, " +
                TYPE + " VARCHAR(16), " +
                EDITABLE + " VARCHAR(8), " +
                VISIBLE + " VARCHAR(8))";
        public static final String SQL_INSERT =
                "INSERT INTO " + TABLE_NAME + "(" +
                NAME + ", " + CHINESE + ", " + VALUE + ", " +
                TYPE + ", " + EDITABLE + ", " + VISIBLE + ") " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        public static final String SQL_UPDATE =
                "UPDATE " + TABLE_NAME + " SET " + VALUE + " = ? " +
                "WHERE " + NAME + " = ?";
        public static final String SQL_DELETE =
                "DELETE FROM " + TABLE_NAME + " WHERE " + NAME + " = ?";
    }
}