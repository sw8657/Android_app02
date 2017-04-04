package com.point.eslee.health_free.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-04-04.
 */

public class RecordDB {

    SQLiteDatabase mDB;
    ContentValues mRow;
    Context mCtx;
    DbOpenHelper mHelper;

    public RecordDB(Context ctx){
        this.mCtx = ctx;
        mHelper = new DbOpenHelper(ctx);
    }

    // 오늘날짜 입력시 이번주 (일-토) 통계 조회
    public ArrayList<Entry> SelectStatStepWeek(String sDate){
        Cursor c = null;
        ArrayList<Entry> entries = new ArrayList<>();
        String sqlString = "";
        int num = 0;
        mDB = mHelper.getReadableDatabase();
        try{
            sqlString = "SELECT SUM(STEPS) as 'STEPS_SUM'"
                    + ", date(C_DATE) as 'C_DATE'"
                    + ", strftime('%w', C_DATE) as 'WEEK'"
                    + "FROM RECORD "
                    + "WHERE USER_ID = '1' "
                    + "AND C_DATE >= date(date('"+ sDate + "', 'localtime', 'weekday 6'),'-6 days') "
                    + "AND C_DATE <= date('" + sDate + "', 'localtime', 'weekday 6')"
                    + "GROUP BY date(C_DATE)"
                    + "ORDER BY strftime('%w', C_DATE);";
            c = mDB.rawQuery(sqlString,null);
            while (c.moveToNext()){
                entries.add(new Entry(c.getFloat(c.getColumnIndex("STEPS_SUM")), num));
                num++;
            }
            c.close();
        }catch (Exception ex){
            Log.e("RecordDB Error : ",ex.getMessage());
        }
        mHelper.close();
        return entries;
    }

    // 해당 날짜기준으로 일요일 날짜와 토요일 날짜를 검색
    // SELECT date(date('2017-02-19', 'weekday 6'),'-6 days'), date('2017-02-19', 'weekday 6');
    public String[] SelectFirstAndEndDate(String sDate){
        String[] sDates = new String[2];
        Cursor c = null;
        String sqlString = "";
        try {
            mDB = mHelper.getReadableDatabase();

            sqlString = "SELECT date(date('"+ sDate + "', 'localtime', 'weekday 6'),'-6 days') as 'DAY1'"
                    + ", date('"+ sDate + "', 'localtime', 'weekday 6') as 'DAY7';";
            c = mDB.rawQuery(sqlString,null);
            if(c != null && c.getCount() != 0){
                c.moveToFirst();
                sDates[0] = c.getString(0);
                sDates[1] = c.getString(1);
            }
            c.close();
        }catch (Exception ex){
            Log.e("RecordDB Error : ",ex.getMessage());
        }
        mHelper.close();
        return sDates;
    }


    // 오늘날짜 입력시 이번달 통계 조회
    public ArrayList<Entry> SelectStatStepMonth(String sDate){
        Cursor c = null;
        ArrayList<Entry> entries = new ArrayList<>();
        String sqlString = "";
        int num = 0;
        mDB = mHelper.getReadableDatabase();
        try{
            sqlString = "SELECT SUM(STEPS) as 'STEPS_SUM'"
                    + ", date(C_DATE) as 'C_DATE'"
                    + ", strftime('%d', C_DATE) as 'DAY'"
                    + "FROM RECORD "
                    + "WHERE USER_ID = '1' "
                    + "AND C_DATE >= date('" + sDate + "', 'localtime', 'start of month') "
                    + "AND C_DATE <= date('" + sDate + "', 'localtime', 'start of month', '+1 month', '-1 day')"
                    + "GROUP BY date(C_DATE)"
                    + "ORDER BY strftime('%d', C_DATE);";
            c = mDB.rawQuery(sqlString,null);
            while (c.moveToNext()){
                entries.add(new Entry(c.getFloat(c.getColumnIndex("STEPS_SUM")), num));
                num++;
            }
            c.close();
        }catch (Exception ex){
            Log.e("RecordDB Error : ",ex.getMessage());
        }
        mHelper.close();
        return entries;
    }


}
