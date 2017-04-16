package com.point.eslee.health_free.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by eslee on 2017-03-01.
 */

public class DbOpenHelper {
    private static final String DATABASE_NAME = "healthfree.db";
    private static final int DATABASE_VERSION = 3;
    private static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper {

        //생성자
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context,name,factory,version);
        }

        // 최초 DB를 만들때 한번만 호출
        @Override
        public void onCreate(SQLiteDatabase db) {
//            db.execSQL(DataBases.UserTable._CREATE);
//            db.execSQL(DataBases.PointTable._CREATE);
//            db.execSQL(DataBases.HealthTable._CREATE);
//            db.execSQL(DataBases.StoreTable._CREATE);
            copyDB();
        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//            db.execSQL("DROP TABLE IF EXISTS " + DataBases.UserTable._TABLENAME);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBases.PointTable._TABLENAME);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBases.HealthTable._TABLENAME);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBases.StoreTable._TABLENAME);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context){
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx,DATABASE_NAME,null,DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public SQLiteDatabase getWritableDatabase(){
        if(mDBHelper == null){
            this.open();
        }
        return mDBHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase(){
        if(mDBHelper == null){
            this.open();
        }
        return mDBHelper.getReadableDatabase();
    }

    public void close(){
        mDB.close();
    }

    // DB가 있나 체크하기
    public boolean isCheckDB() {
        String filePath = "/data/data/" + mCtx.getPackageName() + "/databases/" + DATABASE_NAME;
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    // DB를 복사하기 // assets의 /db/xxxx.db 파일을 설치된 프로그램의 내부 DB공간으로 복사하기
    public void copyDB() {
        Log.d("MiniApp", "copyDB");
        AssetManager manager = mCtx.getAssets();
        String folderPath = "/data/data/" + mCtx.getPackageName() + "/databases";
        String filePath = "/data/data/" + mCtx.getPackageName() + "/databases/" + DATABASE_NAME;
        File folder = new File(folderPath);
        File file = new File(filePath);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            InputStream is = manager.open("db/" + DATABASE_NAME);
            BufferedInputStream bis = new BufferedInputStream(is);
            if (folder.exists()) {
            } else {
                folder.mkdirs();
            }
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = bis.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.flush();
            bos.close();
            fos.close();
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("ErrorMessage : ", e.getMessage());
        }
    }

    // Insert DB
    public long insertUserInfo(String name, String email, String user_type){
        int userid = 1;

        ContentValues values = new ContentValues();
        values.put(DataBases.UserTable.USER_ID, userid);
        values.put(DataBases.UserTable.USER_NAME, name);
        values.put(DataBases.UserTable.EMAIL, email);
        values.put(DataBases.UserTable.USER_TYPE, user_type);
        return mDB.insert(DataBases.UserTable._TABLENAME, null, values);
    }

    // Update DB
    public boolean updateUserInfo(long id , String name, String email){
        ContentValues values = new ContentValues();
        values.put(DataBases.UserTable.USER_NAME, name);
        values.put(DataBases.UserTable.EMAIL, email);
        return mDB.update(DataBases.UserTable._TABLENAME, values, "_ID="+id, null) > 0;
    }

    // Delete ID
    public boolean deleteUserInfo(long id){
        return mDB.delete(DataBases.UserTable._TABLENAME, "_ID="+id, null) > 0;
    }

    // Select All
    public Cursor getAllUserInfo(){
        return mDB.query(DataBases.UserTable._TABLENAME, null, null, null, null, null, null);
    }

    // ID 컬럼 얻어 오기
    public Cursor getUserInfo(long id){
        Cursor c = mDB.query(DataBases.UserTable._TABLENAME, null,
                "_id="+id, null, null, null, null);
        if(c != null && c.getCount() != 0)
            c.moveToFirst();
        return c;
    }

    // 이름 검색 하기 (rawQuery)
    public Cursor getMatchNameFromUserInfo(String name){
        Cursor c = mDB.rawQuery( "SELECT * FROM " + DataBases.UserTable._TABLENAME + " WHERE NAME='" + name + "'" , null);
        return c;
    }

    // Insert DB
    public long insertPoint(int user_id, String use_type, String use_title, int use_point, int total_point,  int store_id){
        ContentValues values = new ContentValues();
        values.put(DataBases.PointTable.USER_ID, user_id);
        values.put(DataBases.PointTable.USE_TYPE, use_type);
        values.put(DataBases.PointTable.USE_TITLE, use_title);
        values.put(DataBases.PointTable.U_POINT, use_point);
        values.put(DataBases.PointTable.T_POINT, total_point);
        values.put(DataBases.PointTable.STORE_ID, store_id);
        return mDB.insert(DataBases.PointTable._TABLENAME, null, values);
    }

    // Update DB
    public boolean updatePoint(long id , String use_type, String use_title, int use_point, int total_point, int store_id){
        ContentValues values = new ContentValues();
        values.put(DataBases.PointTable.USE_TYPE, use_type);
        values.put(DataBases.PointTable.USE_TITLE, use_title);
        values.put(DataBases.PointTable.U_POINT, use_point);
        values.put(DataBases.PointTable.T_POINT, total_point);
        values.put(DataBases.PointTable.STORE_ID, store_id);
        return mDB.update(DataBases.PointTable._TABLENAME, values, "_ID="+id, null) > 0;
    }

    // Delete ID
    public boolean deletePoint(long id){
        return mDB.delete(DataBases.PointTable._TABLENAME, "_ID="+id, null) > 0;
    }

    // Select All
    public Cursor getAllPoint(){
        return mDB.query(DataBases.PointTable._TABLENAME, null, null, null, null, null, null);
    }

    // ID 컬럼 얻어 오기
    public Cursor getPoint(long id){
        Cursor c = mDB.query(DataBases.PointTable._TABLENAME, null,
                "_ID="+id, null, null, null, null);
        if(c != null && c.getCount() != 0)
            c.moveToFirst();
        return c;
    }

}
