package com.cng.android.db;

/**
 * Created by seth.yang on 2016/2/23
 */
public interface DBSchema {
    interface Config {
        String TABLE_NAME = "conf";
        String
                NAME = "_name", VALUE = "_value", CHINESE = "_chinese",
                TYPE = "_type", EDITABLE = "_editable", VISIBLE = "_visible";
        String[] ALL_COLUMNS = {
                NAME, CHINESE, VALUE, TYPE, EDITABLE, VISIBLE
        };
        String DDL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " +
                NAME + " VARCHAR(64) NOT NULL PRIMARY KEY, " +
                CHINESE  + " VARCHAR(256) NOT NULL, " +
                VALUE + " TEXT, " +
                TYPE + " VARCHAR(16), " +
                EDITABLE + " VARCHAR(8), " +
                VISIBLE + " VARCHAR(8))";
        String SQL_INSERT =
                "INSERT INTO " + TABLE_NAME + "(" +
                NAME + ", " + CHINESE + ", " + VALUE + ", " +
                TYPE + ", " + EDITABLE + ", " + VISIBLE + ") " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String SQL_UPDATE =
                "UPDATE " + TABLE_NAME + " SET " + VALUE + " = ? " +
                "WHERE " + NAME + " = ?";
        String SQL_DELETE =
                "DELETE FROM " + TABLE_NAME + " WHERE " + NAME + " = ?";
    }

    interface SensorData {
        String TABLE_NAME = "_sensor_data";
        String TIMESTAMP = "_timestamp", TEMPERATURE = "_temp",
                HUMIDITY = "_humidity", SMOKE = "_smoke";
        String[] ALL_COLUMNS = { TIMESTAMP, TEMPERATURE, HUMIDITY, SMOKE};
        String DDL_CREATE = "" +
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                TIMESTAMP + " BIGINT NOT NULL PRIMARY KEY, " +
                TEMPERATURE + " FLOAT, " +
                HUMIDITY + " FLOAT, " +
                SMOKE  + " FLOAT)";
        String SQL_INSTALL =
                "INSERT INTO " + TABLE_NAME + "(" +
                TIMESTAMP + ", " + TEMPERATURE + ", " + HUMIDITY + ", " +
                SMOKE + ") VALUES (?, ?, ?, ?)";
        String SQL_CLEAR = "TRUNCATE TABLE " + TABLE_NAME;
    }
}