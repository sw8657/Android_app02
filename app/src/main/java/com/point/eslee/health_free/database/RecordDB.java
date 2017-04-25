package com.point.eslee.health_free.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.point.eslee.health_free.VO.RecordVO;
import com.point.eslee.health_free.values;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Administrator on 2017-04-04.
 */

public class RecordDB {

    SQLiteDatabase mDB;
    ContentValues mRow;
    Context mCtx;
    DbOpenHelper mHelper;

    public RecordDB(Context ctx) {
        this.mCtx = ctx;
        mHelper = new DbOpenHelper(ctx);
    }

    // 최종 기록 조회
    public RecordVO SelectLastRecord() {
        RecordVO record = null;
        Cursor c = null;
        String sqlString = "";
        try {
            mDB = mHelper.getReadableDatabase();

            sqlString = "SELECT * FROM "
                    + DataBases.RecordTable._TABLENAME
                    + " WHERE USER_ID = '" + values.UserId + "' ORDER BY _ID DESC "
                    + " LIMIT 1";
            c = mDB.rawQuery(sqlString, null);
            record = new RecordVO();
            if (c != null && c.getCount() != 0) {
                c.moveToFirst();
                record._ID = c.getInt(c.getColumnIndex("_ID"));
                record.Steps = c.getInt(c.getColumnIndex(DataBases.RecordTable.STEPS));
                record.Distance = c.getDouble(c.getColumnIndex(DataBases.RecordTable.DISTANCE));
                record.Calorie = c.getDouble(c.getColumnIndex(DataBases.RecordTable.CALORIE));
                record.TotalPoint = c.getInt(c.getColumnIndex(DataBases.RecordTable.T_POINT));
                record.CreateDate = c.getString(c.getColumnIndex(DataBases.RecordTable.C_DATE));
                record.RunningTime = c.getInt(c.getColumnIndex(DataBases.RecordTable.R_TIME));
            }
            c.close();
        } catch (Exception ex) {
            Log.e("RecordDB Error : ", ex.getMessage());
        }
        mHelper.close();
        return record;
    }

    // 해당 날짜기준으로 일요일 날짜와 토요일 날짜를 검색
    // SELECT date(date('2017-02-19', 'weekday 6'),'-6 days'), date('2017-02-19', 'weekday 6');
    public String[] SelectFirstAndEndDate(String sDate) {
        String[] sDates = new String[2];
        Cursor c = null;
        String sqlString = "";
        try {
            mDB = mHelper.getReadableDatabase();

            sqlString = "SELECT date(date('" + sDate + "', 'localtime', 'weekday 6'),'-6 days') as 'DAY1'"
                    + ", date('" + sDate + "', 'localtime', 'weekday 6') as 'DAY7';";
            c = mDB.rawQuery(sqlString, null);
            if (c != null && c.getCount() != 0) {
                c.moveToFirst();
                sDates[0] = c.getString(0);
                sDates[1] = c.getString(1);
            }
            c.close();
        } catch (Exception ex) {
            Log.e("RecordDB Error : ", ex.getMessage());
        }
        mHelper.close();
        return sDates;
    }

    // 날짜기준 이번주 (일-토) 통계 조회
    public ArrayList<Entry> SelectStatStepWeek(String sDate) {
        Cursor c = null;
        ArrayList<Entry> entries = new ArrayList<>();
        String sqlString = "";
        int num = 0;
        mDB = mHelper.getReadableDatabase();
        try {
            sqlString = "SELECT SUM(STEPS) as 'STEPS_SUM'"
                    + ", date(C_DATE) as 'C_DATE'"
                    + ", strftime('%w', C_DATE) as 'WEEK'"
                    + "FROM RECORD "
                    + "WHERE USER_ID = '" + values.UserId + "' "
                    + "AND C_DATE >= date(date('" + sDate + "', 'localtime', 'weekday 6'),'-6 days') "
                    + "AND C_DATE <= date('" + sDate + "', 'localtime', 'weekday 6')"
                    + "GROUP BY date(C_DATE)"
                    + "ORDER BY strftime('%w', C_DATE);";
            c = mDB.rawQuery(sqlString, null);
            while (c.moveToNext()) {
                entries.add(new Entry(c.getFloat(c.getColumnIndex("STEPS_SUM")), num));
                num++;
            }
            c.close();
        } catch (Exception ex) {
            Log.e("RecordDB Error : ", ex.getMessage());
        }
        mHelper.close();
        return entries;
    }

    // 날짜기준 이번달 통계 조회
    public ArrayList<Entry> SelectStatStepMonth(String sDate) {
        Cursor c = null;
        ArrayList<Entry> entries = new ArrayList<>();
        String sqlString = "";
        int num = 0;
        mDB = mHelper.getReadableDatabase();
        try {
            sqlString = "SELECT SUM(STEPS) as 'STEPS_SUM'"
                    + ", date(C_DATE) as 'C_DATE'"
                    + ", strftime('%d', C_DATE) as 'DAY'"
                    + "FROM RECORD "
                    + "WHERE USER_ID = '" + values.UserId + "' "
                    + "AND C_DATE >= date('" + sDate + "', 'localtime', 'start of month') "
                    + "AND C_DATE <= date('" + sDate + "', 'localtime', 'start of month', '+1 month', '-1 day')"
                    + "GROUP BY date(C_DATE)"
                    + "ORDER BY strftime('%d', C_DATE);";
            c = mDB.rawQuery(sqlString, null);
            while (c.moveToNext()) {
                entries.add(new Entry(c.getFloat(c.getColumnIndex("STEPS_SUM")), num));
                num++;
            }
            c.close();
        } catch (Exception ex) {
            Log.e("RecordDB Error : ", ex.getMessage());
        }
        mHelper.close();
        return entries;
    }

    public boolean UpdateLastRecord(RecordVO recordVO) {
        boolean result = false;
        Cursor c = null;
        String sqlString = "";
        try {
            mDB = mHelper.getWritableDatabase();
            sqlString = "INSERT OR REPLACE INTO RECORD "
                    + " (USER_ID, STEPS, C_DATE, CALORIE, DISTANCE, T_POINT, R_TIME) "
                    + " VALUES (" +
                    values.UserId + ", " +
                    recordVO.Steps + ", " +
                    "DATE('now', 'localtime'), " +
                    recordVO.Calorie + ", " +
                    recordVO.Distance + ", " +
                    recordVO.TotalPoint + ", " +
                    recordVO.RunningTime +
                    ");";
            mDB.execSQL(sqlString);
            Log.d("RecordDB:",sqlString);
        } catch (Exception ex) {
            Log.e("RecordDB Error : ", ex.getMessage());
        }

        return result;
    }

}
