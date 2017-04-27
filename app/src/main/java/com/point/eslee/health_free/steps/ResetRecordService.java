package com.point.eslee.health_free.steps;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.renderscript.Sampler;
import android.util.Log;

import com.point.eslee.health_free.values;

public class ResetRecordService extends Service {
    public ResetRecordService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new ResetRecordTask().execute(); // 초기화 스레드 실행
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class ResetRecordTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            try{
                // 기록 초기화
                Log.i("ResetRecordTask", values.Step + " => 0");
                values.Step = 0;
                values.Distance = 0;
                values.Calorie = 0;
                values.RunningSec = 0;
            }catch (Exception ex){
                Log.e("ResetRecordTask", ex.getMessage());
            }

            ResetRecordService.this.stopSelf();
            return null;
        }
    }
}
