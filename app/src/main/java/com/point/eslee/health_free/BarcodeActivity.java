package com.point.eslee.health_free;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.point.eslee.health_free.VO.MyPointVO;
import com.point.eslee.health_free.VO.StoreVO;
import com.point.eslee.health_free.database.MyPointDB;
import com.point.eslee.health_free.database.StoreDB;

public class BarcodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 타이틀바 제거
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 테두리 제거
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 뒷배경 블러 처리
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.activity_barcode);
    }

    public void onClickBarCode(View view) {
        // 포인트 사용 스레드 실행
        new UsePointTask().execute("Use", -200, 11);
    }

    public class UsePointTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            String msg = "Use Point";
            try {
                if(values.UserId != -1){
                    int store_id = (Integer) params[2];
                    StoreDB storeDB = new StoreDB(getApplicationContext());
                    StoreVO storeVO = storeDB.SelectStoreById(store_id);

                    MyPointVO pointVO = new MyPointVO();
                    pointVO.UseType = (String) params[0]; // Use
                    pointVO.UseTitle = storeVO != null ? storeVO.getStoreName() : "Partner Store"; // 스타벅스 홍대점
                    pointVO.UsePoint = (Integer) params[1]; // use_point
                    pointVO.StoreID = store_id; // 11 // 홍대 스타벅스
                    MyPointDB pointDB = new MyPointDB(getApplicationContext());
                    pointDB.InsertPoint(pointVO);
                    msg = "Use " + pointVO.UsePoint + "Point (" + pointVO.UseTitle + ")";
                }
            } catch (Exception ex) {
                Log.e("UsePointTask", ex.getMessage());
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        }
    }
}
