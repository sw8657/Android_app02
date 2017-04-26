package com.point.eslee.health_free.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.point.eslee.health_free.VO.MyPointVO;
import com.point.eslee.health_free.values;

import java.util.ArrayList;

/**
 * Created by eslee on 2017-04-04.
 */

public class MyPointDB {

    SQLiteDatabase mDB;
    ContentValues mRow;
    Context mCtx;
    DbOpenHelper mHelper;

    public MyPointDB(Context ctx) {
        this.mCtx = ctx;
        mHelper = new DbOpenHelper(ctx);
    }

    // 누적 포인트 검색
    public int SelectTotalPoint() {
        int total_point = 0;
        Cursor c;
        try {
            mDB = mHelper.getReadableDatabase();
            c = mDB.rawQuery("SELECT USER_ID, T_POINT FROM " + DataBases.PointTable._TABLENAME + " WHERE USER_ID = '" + values.UserId + "' ORDER BY _ID DESC LIMIT 1", null);
            if (c != null && c.getCount() != 0) {
                c.moveToFirst();
                total_point = c.getInt(c.getColumnIndex(DataBases.PointTable.T_POINT));
            }
            c.close();
        } catch (Exception ex) {
            Log.e("MyPointDB Error : ", ex.getMessage());
        }
        mHelper.close();
        return total_point;
    }

    // 해당 월의 포인트 내역 조회
    public ArrayList<MyPointVO> SelectPointWhereDate(String Year, String month) {
        ArrayList<MyPointVO> result = new ArrayList<MyPointVO>();
        MyPointVO myPoint = null;
        Cursor c = null;
        String sDate;
        String sqlString = "";

        try {
            mDB = mHelper.getReadableDatabase();
            sDate = Year + "-" + month;
            sqlString = "SELECT * FROM " + DataBases.PointTable._TABLENAME
                    + " WHERE USER_ID = '" + values.UserId + "' "
                    + " AND SUBSTR(C_DATE,0,8) = '" + sDate + "' "
                    + " ORDER BY C_DATE DESC, _ID DESC";
            c = mDB.rawQuery(sqlString, null);
            while (c.moveToNext()) {
                myPoint = new MyPointVO();
                myPoint._ID = c.getInt(c.getColumnIndex(DataBases.PointTable._ID.toUpperCase()));
                myPoint.UseType = c.getString(c.getColumnIndex(DataBases.PointTable.USE_TYPE));
                myPoint.UseTitle = c.getString(c.getColumnIndex(DataBases.PointTable.USE_TITLE));
                myPoint.UsePoint = c.getInt(c.getColumnIndex(DataBases.PointTable.U_POINT));
                myPoint.CreateDate = c.getString(c.getColumnIndex(DataBases.PointTable.C_DATE));
                result.add(myPoint);
            }
            c.close();
        } catch (Exception e) {
            Log.e("MyPointDB Error : ", e.getMessage());
        }
        mHelper.close();
        return result;
    }

    // 포인트 적립
    public void InsertPoint(MyPointVO pointVO) {
        String sqlString = "";
        try {
            mDB = mHelper.getWritableDatabase();
            sqlString = "INSERT INTO " + DataBases.PointTable._TABLENAME
                    + " (USER_ID, USE_TYPE, USE_TITLE, U_POINT, T_POINT, STORE_ID) "
                    + " VALUES ("
                    + values.UserId + ", "
                    + " '" + pointVO.getUseType() + "', "
                    + " '" + pointVO.getUseTitle() + "', "
                    + pointVO.getUsePoint() + ", "
                    + "(SELECT " + pointVO.getUsePoint() + " + T_POINT FROM USE_POINT WHERE USER_ID = '" + values.UserId + "' ORDER BY _ID DESC LIMIT 1), "
                    + pointVO.getStoreID()
                    + ");";
            mDB.execSQL(sqlString);
            Log.d("InsertPoint:",sqlString);
        } catch (Exception e) {
            Log.e("MyPointDB Error : ", e.getMessage());
        }
        mHelper.close();
    }

}
