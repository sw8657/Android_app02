package com.miraens.eslee.test_layout1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment mHomeFragment;
    private Fragment mMyinfoFragment;
    private Fragment mMapFragment;
    private Fragment mStatisticsFragment;
    private SharedPreferences mPref;

    private boolean misLogin = false;
    public String mUserEmail = "Nothing Email";

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        TextView textViewEmail = (TextView) nav_header_view.findViewById(R.id.textViewEmail);
        textViewEmail.setText(mUserEmail);
        TextView textViewName = (TextView) nav_header_view.findViewById(R.id.textViewName);
        textViewName.setText(mPref.getString("example_text", "NothingText"));
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
}
