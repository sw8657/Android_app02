package com.point.eslee.health_free;

import android.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment mHomeFragment;
    private Fragment mMyinfoFragment;
    private Fragment mMapFragment;
    private Fragment mStatisticsFragment;
    private SharedPreferences mPref;
    private PopupWindow mPopupWindow;

    private boolean misLogin = false;
    public String mUserEmail = "Nothing Email";

    Toast mToastWalk;
    Intent intent;
    BroadcastReceiver receiver;
    String serviceData;

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
        mMyinfoFragment = new MyinfoFragment();
        mMapFragment = new MapFragment();
        mStatisticsFragment = new StatisticsFragment();

        // 기본 플래그 화면 설정 (홈)
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_fragment_main, mHomeFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // 환경설정 불러오기
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        // 로그인정보 확인
        if(misLogin == false){
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivityForResult(mainIntent,1004);
        }

        SetNavi_info();

        // 만보기
        intent = new Intent(MainActivity.this, StepBackgroundService.class);
        receiver = new MyMainLocalRecever();

        // 서비스 시작
        if(isServiceRunningCheck() == false){
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1004 && resultCode == RESULT_OK){
            Toast.makeText(MainActivity.this,"로그인 성공!!",Toast.LENGTH_SHORT).show();
            misLogin = true;

            // 로그인창에서 넘어온 로그인정보
            mUserEmail = data.getStringExtra("email");

            SetNavi_info();
        }
    }

    private void SetNavi_info(){
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
        try{
            // 서비스 종료
            unregisterReceiver(receiver);
            stopService(intent);
        }catch (Exception ex){

        }
        super.onDestroy();
    }

    @Override
    protected void onResume(){
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

        if (id == R.id.nav_home){
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

}
