package com.point.eslee.health_free.database;

import android.provider.BaseColumns;

/**
 * Created by eslee on 2017-03-01.
 */

public class DataBases {
    public static final class UserTable implements BaseColumns {
        public static final String USER_ID = "USER_ID";
        public static final String EMAIL = "EMAIL";
        public static final String USER_NAME = "USER_NAME";
        public static final String USER_TYPE = "USER_TYPE";
        public static final String C_DATE = "C_DATE";
        public static final String SEX = "SEX";
        public static final String BIRTHDAY = "BIRTHDAY";
        public static final String HEIGHT = "HEIGHT";
        public static final String WEIGHT = "WEIGHT";
        public static final String _TABLENAME = "USER_INFO";
        public static final String _CREATE =
                "CREATE TABLE " + _TABLENAME+"("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + USER_ID + " INTEGER NOT NULL, "
                        + EMAIL + " TEXT , "
                        + USER_NAME + " TEXT , "
                        + USER_TYPE + " TEXT , "
                        + C_DATE + " DATETIME DEFAULT (datetime('now','localtime')),"
                        + SEX + " TEXT, "
                        + BIRTHDAY + " TEXT, "
                        + HEIGHT + " REAL, "
                        + WEIGHT + " REAL "
                        + " );";
    }

    public static final class PointTable implements BaseColumns{
        public static final String USER_ID = "USER_ID";
        public static final String USE_TYPE = "USE_TYPE";
        public static final String USE_TITLE = "USE_TITLE";
        public static final String U_POINT = "U_POINT";
        public static final String T_POINT = "T_POINT";
        public static final String C_DATE = "C_DATE";
        public static final String STORE_ID = "STORE_ID";
        public static final String _TABLENAME = "USE_POINT";
        public static final String _CREATE =
                "CREATE TABLE " + _TABLENAME+"("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + USER_ID + " INTEGER NOT NULL, "
                        + USE_TYPE + " TEXT , "
                        + USE_TITLE + " TEXT , "
                        + U_POINT + " INTEGER , "
                        + T_POINT + " INTEGER , "
                        + C_DATE + " DATETIME DEFAULT (datetime('now','localtime')) , "
                        + STORE_ID + " INTEGER "
                        + " );";
    }

    public static final class HealthTable implements BaseColumns{
        public static final String USER_ID = "USER_ID";
        public static final String STEPS = "STEPS";
        public static final String C_DATE = "C_DATE";
        public static final String CALORIE = "CALORIE";
        public static final String DISTANCE = "DISTANCE";
        public static final String _TABLENAME = "HEALTH";
        public static final String _CREATE =
                "CREATE TABLE " + _TABLENAME+"("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + USER_ID + " INTEGER NOT NULL, "
                        + STEPS + " INTEGER , "
                        + C_DATE + " DATETIME DEFAULT (datetime('now','localtime')) , "
                        + CALORIE + " REAL , "
                        + DISTANCE + " REAL "
                        + " );";
    }

    public static final class StoreTable implements BaseColumns{
        public static final String STORE_ID = "STORE_ID";
        public static final String STORE_NAME = "STORE_NAME";
        public static final String URL = "URL";
        public static final String X = "X";
        public static final String Y = "Y";
        public static final String C_DATE = "C_DATE";
        public static final String _TABLENAME = "STORE";
        public static final String _CREATE =
                "CREATE TABLE " + _TABLENAME+"("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + STORE_ID + " INTEGER NOT NULL , "
                        + STORE_NAME + " TEXT , "
                        + URL + " TEXT , "
                        + X + " REAL , "
                        + Y + " REAL , "
                        + C_DATE + " DATETIME DEFAULT (datetime('now','localtime')) "
                        + " );";
    }



}
