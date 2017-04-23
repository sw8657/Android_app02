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
import android.content.res.AssetManager;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences mPref;

    public enum Fragments {
        Home,
        MyPoint,
        Map,
        Rank,
        Statistics
    }

    //private boolean misLogin = false;
    public String mUserEmail = "Nothing Email";

    Toast mToastWalk;
    Intent intent;
    BroadcastReceiver receiver;
    String serviceData;

    // 지도
    private LocationManager mLocationManager;
    private CoffeeIntentReceiver mIntentReceiver;
    ArrayList mPendingIntentList;
    String intentKey = "coffeeProximity";

    double old_latitude;
    double old_longitude;
    int i = 0;
    double Sum = 0;

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

        // DB연결 확인
        RecordDB recordDB = new RecordDB(this);
        recordDB.SelectLastRecord();

        // 로그인정보 확인
        if (LoginSharedPreference.isLogin(this) == false) {
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivityForResult(mainIntent, 1004);
        }

        // 샘플 사용자 ID 사용
        values.UserId = 1;
        // 사용자 기록조회 및 만보기 서비스 시작하기
        new UserInfoDoinAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // 환경설정 불러오기
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        // 네비 상단부분 사용자 정보 표시
        SetNavi_info();

        // 지도 서비스 시작
        // 가맹점 위치 서비스 등록
        new StoreRegisterAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // 기본 플래그 화면 홈으로 설정
        replaceFragment(Fragments.Home);
    }

    // 만보기 서비스 시작
    private void StartStepService() {
        // 만보기
        intent = new Intent(MainActivity.this, StepBackgroundService.class);
        receiver = new MyMainLocalRecever();

        // 서비스 시작
        if (isServiceRunningCheck() == false) {
            IntentFilter mainFilter = new IntentFilter("com.eslee.test_layout1");
            registerReceiver(receiver, mainFilter);
            startService(intent);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1004 && resultCode == RESULT_OK) {
            //Toast.makeText(MainActivity.this,"로그인 성공!!",Toast.LENGTH_SHORT).show();
            // 로그인창에서 넘어온 로그인정보
            mUserEmail = data.getStringExtra("email");
            LoginSharedPreference.setLogin(this, mUserEmail);
            SetNavi_info();
        }
    }

    private void SetNavi_info() {
        // 네비뷰 접근
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // 네비뷰 > 상단뷰 접근
        View nav_header_view = navigationView.getHeaderView(0);
        // 네비뷰 > 상단뷰 > 사용자정보뷰에 로그인정보 입력
        ImageView imageViewUser = (ImageView) nav_header_view.findViewById(R.id.imageViewUser);
        BitmapDrawable pDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.img_kongyu);
        RoundedAvatarDrawable pRoundDrawable = new RoundedAvatarDrawable(pDrawable.getBitmap());
        imageViewUser.setImageDrawable(pRoundDrawable);

        TextView textViewEmail = (TextView) nav_header_view.findViewById(R.id.textViewEmail);
        textViewEmail.setText(mUserEmail);
        TextView textViewName = (TextView) nav_header_view.findViewById(R.id.textViewName);
        textViewName.setText(mPref.getString("example_text", "NothingText"));
        textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateRecord();
            }
        });
        imageViewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateRecord();
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            // 서비스 종료
            unregisterReceiver(receiver);
            if(isServiceRunningCheck()){
                stopService(intent);
            }

            // TODO: 종료전 데이터 저장
            UpdateRecord();

        } catch (Exception ex) {

        }
        super.onDestroy();
    }

    private void UpdateRecord() {
        try {
            MyPointDB pointDB = new MyPointDB(this);
            int totalPoint = pointDB.SelectTotalPoint();

            RecordVO recordVO = new RecordVO();
            recordVO.Steps = values.Step;
            recordVO.Distance = values.Distance_sum;
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
        //Toast.makeText(this,"onResume",Toast.LENGTH_SHORT).show();
        SetNavi_info();
        if (mPref != null) {
            int shake_value = Integer.valueOf(mPref.getString("SHAKE_THRESHOLD", "800"));
            StepBackgroundService.setShakeThreshold(shake_value);
        }
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
            if ("com.eslee.test_layout1".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    class MyMainLocalRecever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            serviceData = intent.getStringExtra("serviceData");

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
    private void unregister() {
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

        if (mIntentReceiver != null) {
            unregisterReceiver(mIntentReceiver);
            mIntentReceiver = null;
        }
    }

    /**
     * register the proximity intent receiver
     */
    private void register(int id, double latitude, double longitude, float radius, String name, String url, long expiration) {
        Intent proximityIntent = new Intent(intentKey);
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
            if (i == 0) {
                old_latitude = latitude;
                old_longitude = longitude;

                // MapFragment mapF = (MapFragment) mMapFragment;
                MapFragment mapF = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.content_fragment_map);
                mapF.showCurrentLocation(i, old_latitude, old_longitude, latitude, longitude);
            }

            Location locationS = new Location("point S");
            Location locationE = new Location("point E");
            locationS.setLatitude(Double.parseDouble(Double.toString(old_latitude)));
            locationS.setLongitude(Double.parseDouble(Double.toString(old_longitude)));
            locationE.setLatitude(Double.parseDouble(Double.toString(latitude)));
            locationE.setLongitude(Double.parseDouble(Double.toString(longitude)));
            double distance = locationS.distanceTo(locationE);
            Sum = Sum + distance; //총 이동거리
            values.Distance_sum = Sum;

            old_latitude = latitude;
            old_longitude = longitude;
            i = 1;
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
         * @param intent
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

    public class UserInfoDoinAsyncTask extends AsyncTask<String, Void, String> {
        public String result;
        private Context aContext;
        private RecordDB aRecordDB;
        private RecordVO aRecordVO;

        public UserInfoDoinAsyncTask(Context context) {
            aContext = context;
        }

        @Override
        protected void onPreExecute() {
            aRecordDB = new RecordDB(aContext);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if (aRecordDB == null) aRecordDB = new RecordDB(aContext);
                // 기록 조회
                aRecordVO = aRecordDB.SelectLastRecord();
                // 기록 저장
                Log.i("values update : ", values.Step + " => " + aRecordVO.getSteps());
                values.Step = aRecordVO.getSteps();
                values.Distance_sum = aRecordVO.getDistance();
                values.Calorie = aRecordVO.getCalorie();
                values.RunningSec = aRecordVO.getRunningTime();

            } catch (Exception ex) {
                Log.e("AsyncTask : ", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            StartStepService();
            super.onPostExecute(s);
        }
    }

    public class StoreRegisterAsyncTask extends AsyncTask<String, Void, String> {
        private Context aContext;
        ArrayList<StoreVO> storeVOs = null;
        StoreDB storeDB = null;

        public StoreRegisterAsyncTask(Context context) {
            aContext = context;
        }

        @Override
        protected void onPreExecute() {
            // 지도
            // 일부 단말의 문제로 인해 초기화 코드 추가
            try {
                MapsInitializer.initialize(aContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkDangerousPermissions();
            // 위치 확인하여 위치 표시 시작
            startLocationService();
            // 위치 관리자 객체 참조
            mLocationManager = (LocationManager) aContext.getSystemService(Context.LOCATION_SERVICE);
            mPendingIntentList = new ArrayList();

            storeDB = new StoreDB(aContext);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (storeDB == null) storeDB = new StoreDB(aContext);
            storeVOs = storeDB.SelectAllStore();
            // 가맹점 위치 서비스 등록
            for (StoreVO store : storeVOs) {
                register(store.StoreID, store.Y, store.X, 500, store.StoreName, store.URL, -1);
            }

            // 수신자 객체 생성하여 등록
            mIntentReceiver = new CoffeeIntentReceiver(intentKey);
            registerReceiver(mIntentReceiver, mIntentReceiver.getFilter());
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
        }
    }

}
