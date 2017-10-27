package com.example.mc.assignment2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOperationHelper extends SQLiteOpenHelper {

    private String tableName = "";
    String SQL_QUERY =
            "CREATE TABLE AccelerometerTable " + " (" +
                    DBTableCreator.DBTableEntry._ID + " INTEGER PRIMARY KEY," +
                    DBTableCreator.DBTableEntry.TIME_STAMP + " Real," +
                    DBTableCreator.DBTableEntry.X_COORDINATE + " Real," +
                    DBTableCreator.DBTableEntry.Y_COORDINATE + " Real," +
                    DBTableCreator.DBTableEntry.Z_COORDINATE + " Real" +
                    " )";

    public DBOperationHelper(Context context, String accelerometerTableName) {
        super(context, accelerometerTableName, null, 1);
        tableName = accelerometerTableName;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    }
}
