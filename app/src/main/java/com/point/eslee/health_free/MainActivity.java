package com.point.eslee.health_free;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.point.eslee.health_free.VO.RecordVO;
import com.point.eslee.health_free.VO.StoreVO;
import com.point.eslee.health_free.database.MyPointDB;
import com.point.eslee.health_free.database.RecordDB;
import com.point.eslee.health_free.database.StoreDB;
import com.point.eslee.health_free.point.MypointFragment;
import com.point.eslee.health_free.rank.RankFragment;
import com.point.eslee.health_free.steps.StepBackgroundService;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences mPref;

    public enum Fragments {
        Home,
        MyPoint,
        Map,
        Rank,
        Statistics
    }

    public enum START_TYPE {
        First,
        Continue
    }

    public enum LOGIN_TYPE {
        First,
        Continue
    }

    // 실행정보
    private START_TYPE mStartType = START_TYPE.First;
    private LOGIN_TYPE mLoginType = LOGIN_TYPE.First;

    // 네비뷰 컨트롤뷰
    private ImageView mViewUserImage = null;
    private TextView mViewUserEmail = null;
    private TextView mViewUserName = null;

    private CountDownLatch mLatch = null;

    // 만보기
    Toast mToastWalk;
    Intent m_intent;
    BroadcastReceiver m_receiver_step;
    String m_serviceData;

    // 지도
    private LocationManager mLocationManager;
    private CoffeeIntentReceiver mIntentReceiverMap;
    ArrayList mPendingIntentList;
    String m_mapIntentKey = "coffeeProximity";
    double old_latitude;
    double old_longitude;
    boolean m_animateCamera = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 플로팅액션버튼
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(MainActivity.this, BarcodeActivity.class);
                MainActivity.this.startActivity(mainIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // 네비뷰 레이아웃 로드
        LoadNaviLayout();

        // DB연결 확인
        RecordDB recordDB = new RecordDB(this);
        recordDB.SelectLastRecord();

        // 환경설정 불러오기
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mStartType = mPref.getBoolean("START_FIRST", true) ? START_TYPE.First : START_TYPE.Continue;
        mLoginType = mPref.getBoolean("LOGIN_FIRST", true) ? LOGIN_TYPE.First : LOGIN_TYPE.Continue;

        // 로그인정보 확인
        if (mLoginType.equals(LOGIN_TYPE.First)) {
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivityForResult(mainIntent, 1004);
            Log.i("Main:", "onCreate, 수동로그인");
            Log_value();
        } else {
            Log.i("Main:", "onCreate, 자동로그인");
            Log_value();
            SetStart(mStartType, mLoginType);
        }

        // 기본 플래그 화면 홈으로 설정
        replaceFragment(Fragments.Home);
    }

    private void Log_value() {
        Log.i("values ==> ", "id:" + values.UserId + ", steps:" + values.Step);
    }

    // 로그인 결과
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1004 && resultCode == RESULT_OK) {
            mPref = PreferenceManager.getDefaultSharedPreferences(this);
            // 처음 로그인 된 상태
            Log.i("Main:", "onActivityResult");
            Log_value();
            SetStart(mStartType, LOGIN_TYPE.First);
        }
    }

    // 메인 액티비티 초기화 절차
    private void SetStart(START_TYPE startType, LOGIN_TYPE loginType) {
        if (startType.equals(START_TYPE.First)) {
            // 메인을 처음실행하는 거면
            // 프레프런스에 저장된 아이디를 values에 저장
            mPref = PreferenceManager.getDefaultSharedPreferences(this);
            values.UserId = mPref.getInt("user_id", -1);
            values.UserEmail = mPref.getString("user_email", "Nothing Email");
            values.UserName = mPref.getString("user_name", "Nothing Name");
            // 네비에 사용자 정보 표시
            SetNaviInfo();
            // 만보기 서비스 시작
            StartStepService();
            // 지도 서비스 시작
            StartStoreService();
            if (loginType.equals(LOGIN_TYPE.First)) {

            } else {

            }
        } else {
            // 메인을 처음실행하는게 아니면
            // 프레프런스에 저장된 아이디를 values에 저장
            mPref = PreferenceManager.getDefaultSharedPreferences(this);
            values.UserId = mPref.getInt("user_id", -1);
            values.UserEmail = mPref.getString("user_email", "Nothing Email");
            values.UserName = mPref.getString("user_name", "Nothing Name");
            // 네비에 사용자 정보 표시
            SetNaviInfo();
            if (loginType.equals(LOGIN_TYPE.First)) {
                // 처음 로그인하는 사용자이면
                // 만보기 서비스 종료 후 시작

                StopStepService();
                StartStepService();
                // 지도 서비스 시작
                StartStoreService();
            } else {
                // 로그인 한적이 있으면
                // 지도 서비스 시작
                StartStoreService();
            }
        }
        // 프레프런스에 로그인상태 저장
        mPref.edit().putBoolean("START_FIRST", false).apply();
        mPref.edit().putBoolean("LOGIN_FIRST", false).apply();

//        mLatch = new CountDownLatch(1); // 스레드 작동 카운트
//        // 사용자 기록조회
//        new UserInfoAsyncTask(this).execute();
//        // 스레드 종료 기다리기
//        try {
//            mLatch.await(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Log.i("Main:","SetStart, UserInfoAsyncTask");
//        Log_value();
    }

    // 만보기 서비스 시작
    private void StartStepService() {
        // 서비스 시작
        if (isServiceRunningCheck() == false) {
            // 만보기
            m_intent = new Intent(MainActivity.this, StepBackgroundService.class);
            m_receiver_step = new MyMainLocalRecever();

            IntentFilter mainFilter = new IntentFilter(values.STEP_SERVICE_NAME);
            registerReceiver(m_receiver_step, mainFilter);
            Log.i("Main:", "StartStepService, registerReceiver");
            Log_value();
            startService(m_intent);
            Log.i("Main:", "StartStepService, startService");
            Log_value();
        }
    }

    // 만보기 서비스 중지
    private void StopStepService() {
        if (isServiceRunningCheck()) {
            // 만보기
            if (m_intent != null) {
                stopService(m_intent);
            }
        }
        m_intent = null;
    }

    private void StartStoreService() {
        ArrayList<StoreVO> storeVOs = null;
        StoreDB storeDB = null;
        // 지도 일부 단말의 문제로 인해 초기화 코드 추가
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkDangerousPermissions();
        // 위치 확인하여 위치 표시 시작
        startLocationService();
        // 위치 관리자 객체 참조
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mPendingIntentList = new ArrayList();
        // 가맹점 위치 서비스 등록
        storeDB = new StoreDB(this);
        storeVOs = storeDB.SelectAllStore();
        for (StoreVO store : storeVOs) {
            register(store.StoreID, store.Y, store.X, 500, store.StoreName, store.URL, -1);
        }
        // 수신자 객체 생성하여 등록
        mIntentReceiverMap = new CoffeeIntentReceiver(m_mapIntentKey);
        registerReceiver(mIntentReceiverMap, mIntentReceiverMap.getFilter());
    }

    // 플래그 화면 전환
    public void replaceFragment(Fragments frag) {
        Fragment fragment = null;
        switch (frag) {
            case Home: {
                fragment = new HomeFragment();
                break;
            }
            case MyPoint: {
                fragment = new MypointFragment();
                break;
            }
            case Map: {
                fragment = new MapFragment();
                break;
            }
            case Rank: {
                fragment = new RankFragment();
                break;
            }
            case Statistics: {
                fragment = new StatisticsFragment();
                break;
            }
            default: {
                fragment = new HomeFragment();
                break;
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.content_fragment_main, fragment);
        transaction.replace(R.id.content_fragment_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // 네비뷰 레이아웃 로드
    private void LoadNaviLayout() {
        // 네비뷰 접근
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // 네비뷰 > 상단뷰 접근
        View nav_header_view = navigationView.getHeaderView(0);
        // 네비뷰 > 상단뷰 > 사용자정보뷰에 로그인정보 입력
        mViewUserImage = (ImageView) nav_header_view.findViewById(R.id.imageViewUser);
        mViewUserEmail = (TextView) nav_header_view.findViewById(R.id.textViewEmail);
        mViewUserName = (TextView) nav_header_view.findViewById(R.id.textViewName);

        // 뷰 이벤트 등록
        mViewUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateRecord();
            }
        });
        mViewUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateRecord();
            }
        });
    }

    // 네비뷰 사용자정보 데이터 표출
    private void SetNaviInfo() {
        // 네비뷰 > 상단뷰 > 사용자정보뷰에 로그인정보 입력
        BitmapDrawable pDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.img_kongyu);
        RoundedAvatarDrawable pRoundDrawable = new RoundedAvatarDrawable(pDrawable.getBitmap());
        mViewUserImage.setImageDrawable(pRoundDrawable);

        mViewUserEmail.setText(values.UserEmail);
        mViewUserName.setText(values.UserName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_receiver_step != null){
            unregisterReceiver(m_receiver_step);
            m_receiver_step = null;
        }
        unregister_map();

        try {
            // 종료전 데이터 저장
            UpdateRecord();
            Log.i("Main:", "onDestroy, UpdateRecord");
            Log_value();
        } catch (Exception ex) {

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
    protected void onResume() {
        super.onResume();
        Log.i("Main:", "onResume");
        Log_value();
        SetNaviInfo();
        if (mPref != null) {
            int shake_value = Integer.valueOf(mPref.getString("SHAKE_THRESHOLD", "800"));
            StepBackgroundService.setShakeThreshold(shake_value);
        }
        Log.i("Main:", "onResume, setShakeThreshold");
        Log_value();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent mainIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(mainIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            replaceFragment(Fragments.Home);
        } else if (id == R.id.nav_myinfo) {
            // 내 정보
            replaceFragment(Fragments.MyPoint);
        } else if (id == R.id.nav_map) {
            // 지도
            replaceFragment(Fragments.Map);
        } else if (id == R.id.nav_rank) {
            // 랭킹 (이번주 걸음수, 포인트)
            replaceFragment(Fragments.Rank);
        } else if (id == R.id.nav_stat) {
            // 통계 (오늘 걸음수, 주간, 월간)
            replaceFragment(Fragments.Statistics);
        } else if (id == R.id.nav_settings) {
            // 환경설정
            Intent mainIntent = new Intent(MainActivity.this, SettingsActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            MainActivity.this.startActivity(mainIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (values.STEP_SERVICE_NAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    class MyMainLocalRecever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            m_serviceData = intent.getStringExtra("serviceData");
        }
    }


    private void checkDangerousPermissions() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            //Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                //Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 등록한 정보 해제
     */
    private void unregister_map() {
        if (mPendingIntentList != null) {
            for (int i = 0; i < mPendingIntentList.size(); i++) {
                PendingIntent curIntent = (PendingIntent) mPendingIntentList.get(i);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLocationManager.removeProximityAlert(curIntent);
                mPendingIntentList.remove(i);
            }
        }

        if (mIntentReceiverMap != null) {
            unregisterReceiver(mIntentReceiverMap);
            mIntentReceiverMap = null;
        }
    }

    /**
     * register the proximity m_intent m_receiver_step
     */
    private void register(int id, double latitude, double longitude, float radius, String name, String url, long expiration) {
        Intent proximityIntent = new Intent(m_mapIntentKey);
        proximityIntent.putExtra("id", id);
        proximityIntent.putExtra("latitude", latitude);
        proximityIntent.putExtra("longitude", longitude);
        proximityIntent.putExtra("name", name);
        proximityIntent.putExtra("url", url);
        PendingIntent intent = PendingIntent.getBroadcast(this, id, proximityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.addProximityAlert(latitude, longitude, radius, expiration, intent);
        mPendingIntentList.add(intent);
    }


    /**
     * 현재 위치 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 리스너 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 100;

        try {
            // GPS 기반 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 네트워크 기반 위치 요청
/*            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);*/
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        //Toast.makeText(getActivity().getApplicationContext(), "위치 확인 시작함. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();
    }

    /**
     * 리스너 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인되었을 때 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.i("GPSLocationService", msg);

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
//            showCurrentLocation(latitude, longitude);

            // 현재 위치를 이용해 LatLon 객체 생성
            LatLng newPoint = new LatLng(latitude, longitude);
            if (m_animateCamera) {
                old_latitude = latitude;
                old_longitude = longitude;

                // MapFragment mapF = (MapFragment) mMapFragment;
                MapFragment mapF = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.content_fragment_map);
                mapF.showCurrentLocation(m_animateCamera, old_latitude, old_longitude, latitude, longitude);
            }

            Location locationS = new Location("point S");
            Location locationE = new Location("point E");
            locationS.setLatitude(Double.parseDouble(Double.toString(old_latitude)));
            locationS.setLongitude(Double.parseDouble(Double.toString(old_longitude)));
            locationE.setLatitude(Double.parseDouble(Double.toString(latitude)));
            locationE.setLongitude(Double.parseDouble(Double.toString(longitude)));
            double distance = locationS.distanceTo(locationE);
            values.Distance = values.Distance + distance; //총 이동거리
            Log.i("Main", "onLocationChanged, distance: " + values.Distance);

            old_latitude = latitude;
            old_longitude = longitude;
            m_animateCamera = false;
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    /**
     * 브로드캐스팅 메시지를 받았을 때 처리할 수신자 정의
     */
    private class CoffeeIntentReceiver extends BroadcastReceiver {

        private String mExpectedAction;
        private Intent mLastReceivedIntent;

        public CoffeeIntentReceiver(String expectedAction) {
            mExpectedAction = expectedAction;
            mLastReceivedIntent = null;
        }

        public IntentFilter getFilter() {
            IntentFilter filter = new IntentFilter(mExpectedAction);
            return filter;
        }

        /**
         * 받았을 때 호출되는 메소드
         *
         * @param context
         * @param m_intent
         */
        int j = 0;

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                mLastReceivedIntent = intent;
                j = j + 1;
                int id = intent.getIntExtra("id", 0);
                String name = intent.getStringExtra("name");
                String url = intent.getStringExtra("url");
                double latitude = intent.getDoubleExtra("latitude", 0.0D);
                double longitude = intent.getDoubleExtra("longitude", 0.0D);

                //Toast.makeText(context, "근접한 마커 : " + name, Toast.LENGTH_LONG).show();

                //알림(Notification)을 관리하는 NotificationManager 얻어오기
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                //알림(Notification)을 만들어내는 Builder 객체 생성
                //API 11 버전 이하도 지원하기 위해 NotificationCampat 클래스 사용
                //만약 minimum SDK가 API 11 이상이면 Notification 클래스 사용 가능
                Notification.Builder builder = new Notification.Builder(MainActivity.this);
                //Notification.Builder에게 Notification 제목, 내용, 이미지 등을 설정//////////////////////////////////////
                builder.setSmallIcon(android.R.drawable.ic_menu_myplaces);//상태표시줄에 보이는 아이콘 모양
                builder.setTicker("There is a partner of HeathFree around!"); //알림이 발생될 때 잠시 보이는 글씨
                //상태바를 드래그하여 아래로 내리면 보이는 알림창(확장 상태바)의 아이콘 모양 지정
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_myplaces));

                builder.setContentTitle("There is  " + name + "  around.");    //알림창에서의 제목
                builder.setContentText("Touch it.");   //알림창에서의 글씨
                builder.setVibrate(new long[]{1000, 1000});
                Intent naver = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                PendingIntent pi = PendingIntent.getActivity(MainActivity.this, (int) System.currentTimeMillis(), naver, 0);
                builder.setContentIntent(pi);

                Notification notification = builder.build();   //Notification 객체 생성
                manager.notify(j, notification);             //NotificationManager가 알림(Notification)을 표시

            }
        }

        public Intent getLastReceivedIntent() {
            return mLastReceivedIntent;
        }

        public void clearReceivedIntents() {
            mLastReceivedIntent = null;
        }
    }
}
