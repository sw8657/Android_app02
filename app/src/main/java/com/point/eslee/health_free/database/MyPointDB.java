package com.point.eslee.health_free.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.point.eslee.health_free.VO.MyPointVO;
import java.util.ArrayList;

/**
 * Created by eslee on 2017-04-04.
 */

public class MyPointDB {

    SQLiteDatabase mDB;
    ContentValues mRow;
    Context mCtx;
    DbOpenHelper mHelper;

    public MyPointDB(Context ctx){
        this.mCtx = ctx;
        mHelper = new DbOpenHelper(ctx);
    }

    // 누적 포인트 검색
    public int getTotalPointByUserId(int user_id){
        int total_point = 0;
        Cursor c;
        try{
            mDB = mHelper.getReadableDatabase();
            c =  mDB.rawQuery( "SELECT * FROM " + DataBases.PointTable._TABLENAME + " WHERE USER_ID = '" + user_id + "' ORDER BY _ID DESC" , null);
            if(c != null && c.getCount() != 0){
                c.moveToFirst();
                total_point = c.getInt(c.getColumnIndex(DataBases.PointTable.T_POINT));
            }
            c.close();
        }catch (Exception ex){
            Log.e("MyPointDB Error : ",ex.getMessage());
        }
        mHelper.close();
        return total_point;
    }

    public ArrayList<MyPointVO> SelectPointWhereDate(String Year, String month){
        ArrayList<MyPointVO> result = new ArrayList<MyPointVO>();
        MyPointVO myPoint = null;
        Cursor c;
        String sDate;

        try{
            mDB = mHelper.getReadableDatabase();
            sDate = Year + "-" + month;
            c = mDB.rawQuery( "SELECT * FROM " + DataBases.PointTable._TABLENAME + " WHERE SUBSTR(C_DATE,7) = '" + sDate + "'" , null);
            while (c.moveToNext()){
                myPoint = new MyPointVO();
                myPoint._ID = c.getInt(c.getColumnIndex(DataBases.PointTable._ID));
                myPoint.UseType = c.getString(c.getColumnIndex(DataBases.PointTable.USE_TYPE));
                myPoint.UseTitle = c.getString(c.getColumnIndex(DataBases.PointTable.USE_TITLE));
                myPoint.UsePoint = c.getInt(c.getColumnIndex(DataBases.PointTable.U_POINT));
                myPoint.CreateDate = c.getString(c.getColumnIndex(DataBases.PointTable.C_DATE));
                result.add(myPoint);
            }

        }catch (Exception e){
            Log.e("MyPointDB Error : ",e.getMessage());
        }


        return  result;
    }

}
