package com.point.eslee.health_free.steps;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.point.eslee.health_free.MainActivity;
import com.point.eslee.health_free.VO.RecordVO;
import com.point.eslee.health_free.database.MyPointDB;
import com.point.eslee.health_free.database.RecordDB;
import com.point.eslee.health_free.values;

import java.util.Timer;
import java.util.TimerTask;

public class StepBackgroundService extends Service implements SensorEventListener {

    NotificationManager Notifi_M;
    StepCheckThread thread;
    StepDBThread thread_db;
    Notification Notifi;
    private SharedPreferences mPref;

    public Toast mToastCnt;
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static int SHAKE_THRESHOLD = 800;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    public static void setShakeThreshold(int value){
        SHAKE_THRESHOLD = value;
    }

    public static int getShakeThreshold(){
        return SHAKE_THRESHOLD;
    }

    public StepBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 걸음수 복구
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        // values.Step = mPref.getInt("steps",-1);

        Log.i("StepService", "onCreate ==> steps:" + values.Step);
        LoadRecord();
        Log.i("StepService", "onCreate, LoadRecord ==> steps:" + values.Step);

        mToastCnt = Toast.makeText(StepBackgroundService.this, "걸음수", Toast.LENGTH_SHORT);
        mToastCnt.setGravity(Gravity.TOP, 0, 100);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    // 백그라운드에서 실행되는 동작들이 들어가는 곳입니다.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i("StepService", "onStartCommand ==> steps:" + values.Step);
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);

//        if(thread == null || (thread.isRun == false)){
//            myServiceHandler handler = new myServiceHandler();
//            thread = new StepCheckThread(handler);
//            thread.start();
//        }
//        thread_db = new StepDBThread(handler);
//        thread_db.start();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i("StepService","onTaskRemoved, rootintent:" + rootIntent.toString());
        // 프레프런스에 앱실행상태 저장
        if(mPref == null) mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPref.edit().putBoolean("START_FIRST", true).apply();

        UpdateRecord();
        stopSelf();
    }

    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        if(thread != null && (thread.isRun || thread.isAlive())){
            thread.stopForever();
            thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
        }
        if(thread_db != null && (thread_db.isRun || thread_db.isAlive())){
            thread_db.stopForever();
            thread_db = null;
        }
        Log.i("StepService", "onDestroy ==> steps:" + values.Step);
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        UpdateRecord();
    }

    private void LoadRecord(){
        RecordDB aRecordDB = null;
        RecordVO aRecordVO = null;
        try{
            aRecordDB = new RecordDB(this);
            // 기록 조회
            aRecordVO = aRecordDB.SelectLastRecord();
            // 기록 저장
            Log.i("values update : ", values.Step + " => " + aRecordVO.getSteps());
            values.Step = aRecordVO.getSteps();
            values.Distance = aRecordVO.getDistance();
            values.Calorie = aRecordVO.getCalorie();
            values.RunningSec = aRecordVO.getRunningTime();
        }catch (Exception ex){
            Log.e("LoadRecord:", ex.getMessage());
        }
    }

    private void UpdateRecord() {
        try {
            MyPointDB pointDB = new MyPointDB(this);
            int totalPoint = pointDB.SelectTotalPoint();
            RecordVO recordVO = new RecordVO();
            recordVO.Steps = values.Step;
            recordVO.Distance = values.Distance;
            recordVO.Calorie = values.Calorie;
            recordVO.RunningTime = values.RunningSec;
            recordVO.TotalPoint = totalPoint;
            RecordDB recordDB = new RecordDB(this);
            recordDB.UpdateLastRecord(recordVO);
            Log.d("UpdateRecord: ", "success");
        } catch (Exception ex) {
            Log.e("MainActivity : ", ex.getMessage());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;
                x = sensorEvent.values[SensorManager.DATA_X];
                y = sensorEvent.values[SensorManager.DATA_Y];
                z = sensorEvent.values[SensorManager.DATA_Z];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    values.Step = values.Step + 1;
                    Log.i("StepService", "onSensorChanged ==> steps:" + values.Step);
                    // MainActivity에 값 전달
                    Intent myFilteredResponse = new Intent(values.STEP_SERVICE_NAME);
                    String msg = values.Step + "";
                    myFilteredResponse.putExtra("serviceData", msg);
                    sendBroadcast(myFilteredResponse);
                }

                lastX = sensorEvent.values[DATA_X];
                lastY = sensorEvent.values[DATA_Y];
                lastZ = sensorEvent.values[DATA_Z];
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    class myServiceHandler extends Handler {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(StepBackgroundService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(StepBackgroundService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//            if (MainActivity.mCheckToast) {
//                Notifi = new Notification.Builder(getApplicationContext())
//                        .setContentTitle("Content Title")
//                        .setContentText("Content Text")
//                        .setTicker("알림!!!")
//                        .setContentIntent(pendingIntent)
//                        .build();
//
//                if(MainActivity.mCheckSound){
//                    //소리추가
//                    Notifi.defaults = Notification.DEFAULT_SOUND;
//                    // 환경설정 불러오기
//                    getApplicationContext().getSharedPreferences("notifications_new_message_ringtone",Context.MODE_PRIVATE);
//                    //알림 소리를 한번만 내도록
//                    Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//                }else {
//
//                }
//
//
//                //확인하면 자동으로 알림이 제거 되도록
//                Notifi.flags = Notification.FLAG_AUTO_CANCEL;
//
//                Notifi_M.notify(777, Notifi);
//
//                //토스트 띄우기
//                mToastCnt.setText("걸음수" + values.Step);
//                mToastCnt.show();
//            }

        }
    }

    class StepDBThread extends Thread {
        Handler handler;
        boolean isRun = true;
        private TimerTask mTask;
        private Timer mTimer;
        int cntaa = 0;

        public StepDBThread(Handler handler) {
            this.handler = handler;
        }

        public void stopForever() {
            synchronized (this) {
                this.isRun = false;
                mTimer = null;
            }
        }

        public void run() {
            //반복적으로 수행할 작업을 한다.

            mTask = new TimerTask() {
                @Override
                public void run() {
                    // DB 입력
                    mToastCnt.setText("테스트" + cntaa++);
                    mToastCnt.show();
                }
            };

            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(mTask, 1000, 5000);

            while (isRun) {
                handler.sendEmptyMessage(0);//쓰레드에 있는 핸들러에게 메세지를 보냄
                try {
                    Thread.sleep(10000); //10초씩 쉰다.
                } catch (Exception e) {
                }
            }
        }
    }


}
