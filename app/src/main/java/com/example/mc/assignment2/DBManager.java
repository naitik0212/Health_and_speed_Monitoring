package com.example.mc.assignment2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private static DBManager instance;
    private DBOperationHelper dbOperationHelper;
    private Context context;

    private String accelorometerTableName = "";

    private DBManager() {

    }


    public void saveAccelerometerList(List<AccelerometerData> list) {

        SQLiteDatabase database = openDatabase();
        for (AccelerometerData accelerometerData: list) {

            ContentValues values = new ContentValues();
            values.put(DBTableCreator.DBTableEntry.TIME_STAMP, accelerometerData.getTimestamp());
            values.put(DBTableCreator.DBTableEntry.X_COORDINATE, accelerometerData.getX());
            values.put(DBTableCreator.DBTableEntry.Y_COORDINATE, accelerometerData.getY());
            values.put(DBTableCreator.DBTableEntry.Z_COORDINATE, accelerometerData.getZ());

            long addRow = database.insert("AccelerometerTable",
                    null,
                    values);
        }
        database.close();

    }


    public static DBManager commonInstance() {
        if(instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    public List<AccelerometerData> getAccelerometerData(String databasePath, int count) {
        SQLiteDatabase database;
        if(databasePath != "")
            database = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        else
            database = openDatabase();

        String[] values = {
                DBTableCreator.DBTableEntry._ID,
                DBTableCreator.DBTableEntry.TIME_STAMP,
                DBTableCreator.DBTableEntry.X_COORDINATE,
                DBTableCreator.DBTableEntry.Y_COORDINATE,
                DBTableCreator.DBTableEntry.Z_COORDINATE,
        };
        List<AccelerometerData> listOfValues = new ArrayList<>();

        Cursor cursor = database.query(
                "AccelerometerTable",
                values,
                null,
                null,
                null,
                null,
                DBTableCreator.DBTableEntry.TIME_STAMP+" DESC",
                Integer.toString(count)
        );

        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {

            AccelerometerData acceleromterData = new AccelerometerData();

            long timestamp = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBTableCreator.DBTableEntry.TIME_STAMP)
            );
            float x = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(DBTableCreator.DBTableEntry.X_COORDINATE)
            );
            float y = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(DBTableCreator.DBTableEntry.Y_COORDINATE)
            );
            float z = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(DBTableCreator.DBTableEntry.Z_COORDINATE)
            );
            acceleromterData.setTimestamp(timestamp);
            acceleromterData.setX(x);
            acceleromterData.setY(y);
            acceleromterData.setZ(z);
            listOfValues.add(acceleromterData);

        }

        cursor.close();
        database.close();
        return listOfValues;
    }

    public boolean isDatabaseAvialable() {

        return (dbOperationHelper != null);
    }

    //db path
    public String databasePath() {
        if (accelorometerTableName == null || context == null)
            return null;
        File file = context.getDatabasePath(accelorometerTableName);
        return file.getAbsolutePath();

    }

    //dbname
    public String databaseName() {
        if (accelorometerTableName == null || context == null)
            return null;
        File file = context.getDatabasePath(accelorometerTableName);
        return file.getName();
    }

    //opendb
    private SQLiteDatabase openDatabase() {
        if (dbOperationHelper != null) {
            SQLiteDatabase db = dbOperationHelper.getWritableDatabase();
            return db;
        }
        return null;
    }

    private void initializeDatabase(String tableName, Context context) {
        this.context = context;
        dbOperationHelper = new DBOperationHelper(context, tableName);
    }

    //create table
    public void createTable(String name, Context context) {
        dbOperationHelper = null;
        accelorometerTableName = name;
        initializeDatabase(name, context);
    }





}
