package com.example.mc.assignment2;

import android.provider.BaseColumns;

public class DBTableCreator {

    public DBTableCreator() {
    }

    public static abstract  class DBTableEntry implements BaseColumns {

        public static final String TIME_STAMP = "timestamp";
        public static final String X_COORDINATE = "xcoordinate";
        public static final String Y_COORDINATE = "ycoordinate";
        public static final String Z_COORDINATE = "zcoordinate";
    }
}
