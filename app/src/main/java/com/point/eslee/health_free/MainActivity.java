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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment mHomeFragment;
    private Fragment mMyinfoFragment;
    private Fragment mMapFragment;
    private Fragment mStatisticsFragment;
    private SharedPreferences mPref;
    private PopupWindow mPopupWindow;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

//                View popupView = getLayoutInflater().inflate(R.layout.activity_barcode,null);
//                mPopupWindow = new PopupWindow(popupView);
//                mPopupWindow.setWindowLayoutMode(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
//
//                // 팝업 터치 가능
//                mPopupWindow.setTouchable(true);
//                mPopupWindow.setFocusable(true);
//                // 팝업 외부 터치 가능(외부터치 나갈수있게)
//                mPopupWindow.setOutsideTouchable(true);
//                // 외부터치 인식을 위한 추가 설정
//                mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
//                // 애니메이션 활성화
//                mPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);
//                // 한가운데 팝업 생성
//                mPopupWindow.showAtLocation(popupView, Gravity.BOTTOM,0,10);

                Intent mainIntent = new Intent(MainActivity.this, BarcodeActivity.class);
                MainActivity.this.startActivity(mainIntent);

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Fragment 생성
        mHomeFragment = new HomeFragment();
        mMyinfoFragment = new MypointFragment();
        mMapFragment = new MapFragment();
        mStatisticsFragment = new StatisticsFragment();

        // 기본 플래그 화면 지도 떴다가
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_fragment_main, mHomeFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // 환경설정 불러오기
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        // 로그인정보 확인
        if (LoginSharedPreference.isLogin(this) == false) {
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivityForResult(mainIntent, 1004);
        }

        SetNavi_info();

        // 만보기
        intent = new Intent(MainActivity.this, StepBackgroundService.class);
        receiver = new MyMainLocalRecever();

        // 서비스 시작
        if (isServiceRunningCheck() == false) {
            IntentFilter mainFilter = new IntentFilter("com.eslee.test_layout1");
            registerReceiver(receiver, mainFilter);
            startService(intent);
        }

        // 지도
        // 일부 단말의 문제로 인해 초기화 코드 추가
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

        register(1001, 37.5571895, 126.923642, 500, "스타벅스 홍대역점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9872", -1);
        register(1002, 37.5568004, 126.9199674, 500, "스타벅스 동교점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9837", -1);
        register(1003, 37.5532579, 126.9248262, 500, "스타벅스 홍대갤러리점", "http://www.istarbucks.co.kr", -1);
        register(1004, 37.5518991, 126.9232424, 500, "스타벅스 홍대공원점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9986", -1);
        register(1005, 37.558898, 126.9275124, 500, "스타벅스 동교삼거리점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9888", -1);
        register(1006, 37.5529804, 126.9218637, 500, "스타벅스 홍대로데오점", "http://www.istarbucks.co.kr", -1);
        register(1007, 37.5501915, 126.9232343, 500, "스타벅스 홍대삼거리점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9602", -1);
        register(1008, 37.5513451, 126.9169083, 500, "스타벅스 서교점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=3056", -1);
        register(1009, 37.5533397, 126.918578, 500, "스타벅스 서교동사거리점", "http://www.istarbucks.co.kr", -1);
        register(1010, 37.5523765, 126.9377746, 500, "스타벅스 서강대점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9983", -1);
        register(1011, 37.55649, 126.9371201, 500, "스타벅스 신촌점", "http://www.istarbucks.co.kr", -1);
        register(1012, 37.5586535, 126.9366775, 500, "스타벅스 연대점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9639", -1);
        register(1013, 37.5587566, 126.9402234, 500, "스타벅스 신촌기차역점", "http://www.istarbucks.co.kr", -1);
        register(1014, 37.5577519, 126.9381461, 500, "스타벅스 신촌명물거리점", "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9530", -1);
        register(1015, 37.5561264, 126.9392538, 500, "스타벅스 신촌대로점", "http://www.istarbucks.co.kr", -1);
        register(1016, 37.5584238, 126.9265576, 500, "카페베네 동교동로터리점", "http://www.caffebene.co.kr", -1);
        register(1017, 37.5567823, 126.9199795, 500, "카페베네 동교중앙점", "http://www.caffebene.co.kr", -1);
        register(1018, 37.5546741, 126.9218087, 500, "카페베네 홍대역점", "http://www.caffebene.co.kr", -1);
        register(1019, 37.5592297, 126.9398063, 500, "카페베네 신촌점", "http://www.caffebene.co.kr", -1);
        register(1020, 37.557535, 126.9190876, 500, "파리바게트 서교점", "http://www.paris.co.kr", -1);
        register(1021, 37.5557298, 126.9203644, 500, "파리바게트 홍대점", "http://www.paris.co.kr", -1);
        register(1022, 37.5516471, 126.9163089, 500, "파리바게트 합정역점", "http://www.paris.co.kr", -1);
        register(1023, 37.5530505, 126.933188, 500, "파리바게트 마포창천", "http://www.paris.co.kr", -1);
        register(1024, 37.5585631, 126.9278075, 500, "파리바게트", "http://www.paris.co.kr", -1);
        register(1025, 37.5554523, 126.9233054, 500, "커피빈 홍대역점", "http://www.coffeebeankorea.com", -1);
        register(1026, 37.5585482, 126.9367567, 500, "맥도날드 연세대점", "http://www.mcdonalds.co.kr", -1);
        register(1027, 37.5556182, 126.937167, 500, "맥도날드 신촌점", "http://www.mcdonalds.co.kr", -1);
        register(1028, 37.5550759, 126.9219723, 500, "맥도날드 홍익대점", "http://www.mcdonalds.co.kr", -1);
        register(1029, 37.5560626, 126.9097254, 500, "맥도날드 망원점", "http://www.mcdonalds.co.kr", -1);
        register(1030, 37.5529559, 126.9238713, 500, "롯데리아 홍대점", "http://www.lotteria.com", -1);
        register(1031, 37.554606, 126.9356838, 500, "롯데리아 신촌로터리점", "http://www.lotteria.com", -1);
        register(1032, 37.5556735, 126.9235575, 500, "버거킹 홍대역점", "http://www.burgerking.co.kr", -1);
        register(1033, 37.555599, 126.9372851, 500, "버거킹 신촌점", "http://www.burgerking.co.kr", -1);
        register(1034, 37.5559796, 126.9351688, 500, "KFC 신촌점", "http://www.kfckorea.com", -1);
        register(1035, 37.5559796, 126.923115, 500, "KFC 홍대점", "http://www.kfckorea.com", -1);
        register(1036, 37.5556458, 126.936585, 500, "투썸플레이스 신촌점", "https://www.twosome.co.kr", -1);
        register(1037, 37.5511761, 126.9392511, 500, "투썸플레이스 서강대점", "https://www.twosome.co.kr", -1);
        register(1038, 37.558814, 126.9419172, 500, "투썸플레이스 신촌기차역점", "https://www.twosome.co.kr", -1);

        // 수신자 객체 생성하여 등록
        mIntentReceiver = new CoffeeIntentReceiver(intentKey);
        registerReceiver(mIntentReceiver, mIntentReceiver.getFilter());

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
    }

    @Override
    protected void onDestroy() {
        try {
            // 서비스 종료
            unregisterReceiver(receiver);
            stopService(intent);
        } catch (Exception ex) {

        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this,"onResume",Toast.LENGTH_SHORT).show();
        SetNavi_info();
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

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            transaction.replace(R.id.content_fragment_main, mHomeFragment);
        } else if (id == R.id.nav_myinfo) {
            // 내 정보
            transaction.replace(R.id.content_fragment_main, mMyinfoFragment);
        } else if (id == R.id.nav_map) {
            // 지도
            transaction.replace(R.id.content_fragment_main, mMapFragment);
        } else if (id == R.id.nav_stat) {
            // 통계 (오늘 걸음수, 주간, 월간)
            transaction.replace(R.id.content_fragment_main, mStatisticsFragment);
        } else if (id == R.id.nav_settings) {
            // 환경설정
            Intent mainIntent = new Intent(MainActivity.this, SettingsActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            MainActivity.this.startActivity(mainIntent);
        }

        transaction.addToBackStack(null);
        transaction.commit();

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

                MapFragment mapF = (MapFragment) mMapFragment;
                // MapFragment mapF = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.content_fragment_map);
                mapF.showCurrentLocation(i,old_latitude,old_longitude, latitude,longitude);
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
                j=j+1;
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
                builder.setVibrate(new long[] { 1000, 1000 });
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
