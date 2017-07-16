package com.point.eslee.health_free.steps;

import android.annotation.TargetApi;
import android.app.AlarmManager;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.point.eslee.health_free.Common;
import com.point.eslee.health_free.MainActivity;
import com.point.eslee.health_free.VO.MyPointVO;
import com.point.eslee.health_free.VO.RecordVO;
import com.point.eslee.health_free.database.MyPointDB;
import com.point.eslee.health_free.database.RankDB;
import com.point.eslee.health_free.database.RecordDB;
import com.point.eslee.health_free.values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class StepBackgroundService extends Service implements SensorEventListener {

    NotificationManager Notifi_M;
    StepCheckThread thread;
    StepDBThread thread_db;
    Notification Notifi;
    private SharedPreferences mPref;

    // 포인트 적립
    ArrayList<Integer> STEP_POINTs = new ArrayList<>(Arrays.asList(1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000));

    // 운동시간 체크
    Timer m_runningTimer;
    Timer m_updateRankTimer;
    Timer m_datecheckTimer;
    private int m_runstep = 0;
    private Calendar m_runStartTime = null;
    private boolean m_isRunning = false;

    // 스타트 체크
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

    public static void setShakeThreshold(int value) {
        SHAKE_THRESHOLD = value;
    }

    public static int getShakeThreshold() {
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

        // 운동 시간 체크 타이머
        m_runningTimer = new Timer();
        m_runningTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i("runTimer S", "Running:" + m_isRunning + ", runstep:" + m_runstep + ", second:" + values.RunningSec + ", calorie:" + values.Calorie);
                // 5초동안 걸음수가 5을 넘었으면 운동시작
                if (m_runstep > 5) {
                    if (!m_isRunning) {
                        // 운동중이 아니면
                        m_runStartTime = Calendar.getInstance(); // 운동시작시간 기록
                    }
                    m_isRunning = true;
                } else {
                    // 운동종료
                    int iRunningSec = Common.getRunningTimeSecond(m_runStartTime); // 운동시간계산
                    int iCalorie = Common.convertSecToCalorie(iRunningSec); // 칼로리계산
                    // 운동시간 기록
                    values.RunningSec += iRunningSec;
                    values.Calorie += iCalorie;
                    // 운동체크 FLAG 초기화
                    m_runStartTime = null;
                    m_isRunning = false;
                }
                // 10초마다 걸음수체크 초기화
                m_runstep = 0;
                Log.i("runTimer E", "Running:" + m_isRunning + ", runstep:" + m_runstep + ", second:" + values.RunningSec + ", calorie:" + values.Calorie);
            }
        }, 0, 5000); // 5초마다 실행

        // 사용자 랭킹정보 서버에 업데이트 실행 타이머
        m_updateRankTimer = new Timer();
        m_updateRankTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new UpdateUserRankInfo().execute((Void) null);
            }
        }, 0, 1000 * 60 * 10); // 10분마다 실행
    }

    // 백그라운드에서 실행되는 동작들이 들어가는 곳입니다.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i("StepService", "onStartCommand ==> steps:" + values.Step);
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);

        // 로그인하면 저장된 기록 복구
        boolean isLoadRecord = intent.getBooleanExtra("load_record", false);
        if (isLoadRecord) {
            LoadRecord();
        }

        //  기록 초기화 알람 등록
        registerResetRecordAlarm(getApplicationContext());
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
        Log.i("StepService", "onTaskRemoved, rootintent:" + rootIntent.toString());
        // 프레프런스에 앱실행상태 저장
        if (mPref == null) mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPref.edit().putBoolean("START_FIRST", true).apply();

        UpdateRecord();
        stopSelf();
    }

    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        if (thread != null && (thread.isRun || thread.isAlive())) {
            thread.stopForever();
            thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
        }
        if (thread_db != null && (thread_db.isRun || thread_db.isAlive())) {
            thread_db.stopForever();
            thread_db = null;
        }
        m_runningTimer = null;

        Log.i("StepService", "onDestroy ==> steps:" + values.Step);
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        UpdateRecord();
    }

    private void LoadRecord() {
        RecordDB aRecordDB = null;
        RecordVO aRecordVO = null;
        try {
            aRecordDB = new RecordDB(this);
            // 오늘일자 마지막 기록조회
            aRecordVO = aRecordDB.SelectTodayRecord();
            if (aRecordVO != null) {
                // 기록 복구
                Log.i("values update : ", values.Step + " => " + aRecordVO.getSteps());
                values.Step = aRecordVO.getSteps();
                values.Distance = aRecordVO.getDistance();
                values.Calorie = aRecordVO.getCalorie();
                values.RunningSec = aRecordVO.getRunningTime();
            } else {
                // 기록 초기화
                Log.i("values clear : ", values.Step + " => 0");
                values.Step = 0;
                values.Distance = 0;
                values.Calorie = 0;
                values.RunningSec = 0;
            }
        } catch (Exception ex) {
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

    public void registerResetRecordAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 돌아오는 자정
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Intent intent = new Intent(context, ResetRecordService.class);
        PendingIntent sender = PendingIntent.getService(context, 2400, intent, 0);
        // 트리거 시간 : 돌아오는 자정
        // 반복 주기 : 하루
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
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
                    values.Step += 1; // 걸음수
                    m_runstep++; // 운동하는지 확인하는 걸음수
                    Log.i("StepService", "onSensorChanged ==> steps:" + values.Step);
                    // MainActivity에 값 전달
                    Intent myFilteredResponse = new Intent(values.STEP_SERVICE_NAME);
                    String msg = values.Step + "";
                    myFilteredResponse.putExtra("serviceData", msg);
                    sendBroadcast(myFilteredResponse);

                    // 포인트 적립 - 걸음수 달성마다 포인트
                    if (STEP_POINTs.contains(values.Step)) {
                        int steps = values.Step;
                        int point = 100; // steps / 10; // 10% 포인트 적립
                        new InsertPointUpTask().execute("Save Walking", steps + " steps", point);
                    }
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

    // 랭킹전 정보 서버에 업데이트
    public class UpdateUserRankInfo extends AsyncTask<Void, Void, Boolean> {
        Integer step, Calorie, r_time, t_point;
        Double distance;
        Boolean iReady = false;

        @Override
        protected void onPreExecute() {
            try {
                step = values.Step;
                Calorie = values.Calorie;
                r_time = values.RunningSec;
                distance = values.Distance;
                t_point = new MyPointDB(getApplicationContext()).SelectTotalPoint();
                iReady = true;
            } catch (Exception ex) {

            }

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (iReady) {
                new RankDB(getApplicationContext()).UpdateRankInfo(step, distance, Calorie, t_point, r_time);
            }
            return null;
        }
    }

    // 걸은만큼 포인트 적립
    public class InsertPointUpTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            try {
                MyPointVO pointVO = new MyPointVO();
                pointVO.UseType = (String) params[0]; // Save Walking
                pointVO.UseTitle = (String) params[1]; // 1000 steps
                pointVO.UsePoint = (Integer) params[2]; // point
                MyPointDB pointDB = new MyPointDB(getApplicationContext());
                pointDB.InsertPoint(pointVO);
            } catch (Exception ex) {
                Log.e("STEP_SERVICE", ex.getMessage());
            }
            return null;
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
                    mToastCnt.setText("50포인트 획득" + cntaa++);
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
