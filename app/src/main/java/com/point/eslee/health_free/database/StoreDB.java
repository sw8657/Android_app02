package com.point.eslee.health_free.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.point.eslee.health_free.VO.StoreVO;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-04-05.
 */

public class StoreDB {

    SQLiteDatabase mDB;
    ContentValues mRow;
    Context mCtx;
    DbOpenHelper mHelper;

    public StoreDB(Context ctx) {
        this.mCtx = ctx;
        mHelper = new DbOpenHelper(ctx);
    }

    // 가맹점 전체 조회
    public ArrayList<StoreVO> SelectAllStore() {
        ArrayList<StoreVO> result = new ArrayList<StoreVO>();
        StoreVO store = null;
        Cursor c;

        try {
            mDB = mHelper.getReadableDatabase();
            c = mDB.query(DataBases.StoreTable._TABLENAME, null, null, null, null, null, null);
            while (c.moveToNext()) {
                store = new StoreVO();
                store._ID = c.getInt(c.getColumnIndex(DataBases.StoreTable._ID.toUpperCase()));
                store.StoreID = c.getInt(c.getColumnIndex(DataBases.StoreTable.STORE_ID));
                store.StoreName = c.getString(c.getColumnIndex(DataBases.StoreTable.STORE_NAME));
                store.URL = c.getString(c.getColumnIndex(DataBases.StoreTable.URL));
                store.X = c.getDouble(c.getColumnIndex(DataBases.StoreTable.X));
                store.Y = c.getDouble(c.getColumnIndex(DataBases.StoreTable.Y));
                store.CreateDate = c.getString(c.getColumnIndex(DataBases.StoreTable.C_DATE));
                result.add(store);
            }
            c.close();
        } catch (Exception e) {
            Log.e("StoreDB : ", e.getMessage());
        }
        if (mHelper != null) mHelper.close();
        return result;
    }

    // 가맹점 전체 조회
    public ArrayList<StoreVO> SelectAllStore2() {
        ArrayList<StoreVO> result = new ArrayList<StoreVO>();
        StoreVO store = null;
        Cursor c;
        String sqlString = "";

        try {
            mDB = mHelper.getReadableDatabase();
            sqlString = "SELECT * FROM " + DataBases.StoreTable._TABLENAME + " ORDER BY STORE_ID";
            c = mDB.rawQuery(sqlString, null);
            while (c.moveToNext()) {
                store = new StoreVO();
                store._ID = c.getInt(c.getColumnIndex("_ID"));
                store.StoreID = c.getInt(c.getColumnIndex(DataBases.StoreTable.STORE_ID));
                store.StoreName = c.getString(c.getColumnIndex(DataBases.StoreTable.STORE_NAME));
                store.URL = c.getString(c.getColumnIndex(DataBases.StoreTable.URL));
                store.X = c.getDouble(c.getColumnIndex(DataBases.StoreTable.X));
                store.Y = c.getDouble(c.getColumnIndex(DataBases.StoreTable.Y));
                store.CreateDate = c.getString(c.getColumnIndex(DataBases.StoreTable.C_DATE));
                result.add(store);
            }
            c.close();
        } catch (Exception e) {
            Log.e("StoreDB : ", e.getMessage());
        }
        if (mHelper != null) mHelper.close();
        return result;
    }

    // 가맹점 ID로 조회
    public StoreVO SelectStoreById(int store_id) {
        StoreVO store = null;
        Cursor c;
        String sqlString = "";

        try {
            mDB = mHelper.getReadableDatabase();
            sqlString = "SELECT * FROM " + DataBases.StoreTable._TABLENAME
                    + " WHERE STORE_ID = " + store_id;
            c = mDB.rawQuery(sqlString, null);
            if(c != null && c.getCount() != 0){
                c.moveToFirst();
                store = new StoreVO();
                store._ID = c.getInt(c.getColumnIndex("_ID"));
                store.StoreID = c.getInt(c.getColumnIndex(DataBases.StoreTable.STORE_ID));
                store.StoreName = c.getString(c.getColumnIndex(DataBases.StoreTable.STORE_NAME));
                store.URL = c.getString(c.getColumnIndex(DataBases.StoreTable.URL));
                store.X = c.getDouble(c.getColumnIndex(DataBases.StoreTable.X));
                store.Y = c.getDouble(c.getColumnIndex(DataBases.StoreTable.Y));
                store.CreateDate = c.getString(c.getColumnIndex(DataBases.StoreTable.C_DATE));
            }
            c.close();
        } catch (Exception e) {
            Log.e("StoreDB : ", e.getMessage());
        }
        if (mHelper != null) mHelper.close();
        return store;
    }

}
