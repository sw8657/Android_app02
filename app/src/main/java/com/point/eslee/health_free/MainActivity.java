package com.point.eslee.health_free;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoginStatusCallback;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.point.eslee.health_free.VO.RecordVO;
import com.point.eslee.health_free.VO.StoreVO;
import com.point.eslee.health_free.database.MyPointDB;
import com.point.eslee.health_free.database.RecordDB;
import com.point.eslee.health_free.database.StoreDB;
import com.point.eslee.health_free.point.MypointFragment;
import com.point.eslee.health_free.rank.RankFragment;
import com.point.eslee.health_free.steps.StepBackgroundService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    private Uri mImageCaptureUri;
    private int id_view;
    private String absolutePath;

    private LoginManager mLoginManager;
    private AccessToken mAccessToken;
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

    // 플로팅액션버튼
    FloatingActionButton mFab = null;

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
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 플로팅액션버튼
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
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

        mToastWalk = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        // 네비뷰 레이아웃 로드
        LoadNaviLayout();

        try {
            // DB연결 확인
            RecordDB recordDB = new RecordDB(this);
            recordDB.SelectLastRecord();
        } catch (Exception ex) {
            Log.e("DB test", ex.getMessage());
        }

        // 환경설정 불러오기
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mStartType = mPref.getBoolean("START_FIRST", true) ? START_TYPE.First : START_TYPE.Continue;
        mLoginType = mPref.getBoolean("LOGIN_FIRST", true) ? LOGIN_TYPE.First : LOGIN_TYPE.Continue;

        mAccessToken = AccessToken.getCurrentAccessToken();
        if (mAccessToken == null || mAccessToken.isExpired()) {
            Log.i("Main:", "mLoginManager, 토큰없음");
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
        } else {
            Log.i("Main:", "mLoginManager, 토큰있음");
            GraphRequest request = GraphRequest.newMeRequest(mAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    String fb_id = "";
                    String email = "";
                    String user_name = "";
                    String sex = "";
                    String birthday = "";
                    String friends = "";
                    try {
                        // 로그인정보 확인
                        // 사용자정보 갱신

                        if (mLoginType.equals(LOGIN_TYPE.First)) {
                            LoginManager.getInstance().logOut(); // 페이스북 로그아웃
                            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
                            MainActivity.this.startActivityForResult(mainIntent, 1004);
                            Log.i("Main:", "onCreate, 수동로그인");
                            Log_value();
                        } else {
                            Log.i("Main:", "onCreate, 자동로그인");
                            Log_value();
                            SetStart(mStartType, mLoginType);

                            fb_id = object.getString("id");
                            email = object.isNull("email") ? fb_id + "@test.com" : object.getString("email");
                            user_name = object.isNull("name") ? "username" : object.getString("name");
                            sex = object.isNull("gender") ? "male" : object.getString("gender");
                            birthday = object.isNull("birthday") ? "20170101" : object.getString("birthday");
                            ArrayList<String> friendsList = new ArrayList<String>();
                            if (object.isNull("friends") == false) {
                                JSONArray pFriends = object.getJSONObject("friends").getJSONArray("data");
                                for (int i = 0; i < pFriends.length(); i++) {
                                    friendsList.add("'" + pFriends.getJSONObject(i).getString("id") + "'");
                                }
                            }
                            friends = TextUtils.join(",", friendsList);

                            // TODO : 친구목록 갱신
                        }
                    } catch (Exception ex) {

                    }
                }
            });
            Bundle param = new Bundle();
            param.putString("fields", "id,name,email,gender,birthday,friends");
            request.setParameters(param);
            request.executeAsync();
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

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case 1004: {
                mPref = PreferenceManager.getDefaultSharedPreferences(this);
                // 처음 로그인 된 상태
                Log.i("Main:", "onActivityResult");
                Log_value();
                SetStart(mStartType, LOGIN_TYPE.First);
                break;
            }
            case PICK_FROM_ALBUM: {
                // 이후의 처리가 카메라와 같으므로 일단 break 없이 진행
                mImageCaptureUri = data.getData();
                Log.d("ImageCatureUri", mImageCaptureUri.getPath().toString());
            }
            case PICK_FROM_CAMERA: {
                // 이미지를 가져온 이후 리사이즈할 이미지 크기를 결정
                // 이후에 이미지 크롭어플리케이션을 호출
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {

                } else {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                // CROP할 이미지를 200*200 크기로 저장
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_IMAGE);
                break;
            }
            case CROP_FROM_IMAGE: {
                // 크롭된 이후의 이미지를 넘겨 받습니다.
                if (resultCode != RESULT_OK) return;
                final Bundle extras = data.getExtras();

                // CROP된 이미지를 저장하기 위한 FILE경로
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HealthFree/" + System.currentTimeMillis() + ".jpg";

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data"); //CROP된 BITMAP
                    storeCropImage(photo, filePath); // CROP된 이미지를 외부저장소, 앨범에 저장한다.
                    absolutePath = filePath;

                    Glide.with(this).load(Uri.fromFile(new File(absolutePath)))
                            .bitmapTransform(new CropCircleTransformation(this))
                            .placeholder(R.drawable.blank_profile)
                            .error(R.drawable.blank_profile)
                            .into(mViewUserImage);

                    mPref.edit().putString("userImage", absolutePath).apply();

                    break;
                }

                // 임시 파일 삭제
                File f = new File(mImageCaptureUri.getPath());
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    private void storeCropImage(Bitmap bitmap, String filePath) {
        // HealthFree 폴더를 생성하여 이미지를 저장하는 방식
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HealthFree";
        File directory_HealthFree = new File(dirPath);

        if (!directory_HealthFree.exists()) {
            directory_HealthFree.mkdir();
        }

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;
        try {
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            // sandBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

            out.flush();
            out.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 메인 액티비티 초기화 절차
    private void SetStart(START_TYPE startType, LOGIN_TYPE loginType) {
        if (startType.equals(START_TYPE.First)) {
            // 메인을 처음실행하는 거면
            // 프레프런스에 저장된 아이디를 values에 저장
            mPref = PreferenceManager.getDefaultSharedPreferences(this);
            values.UserId = mPref.getInt("user_id", -1);
            values.FacebookId = mPref.getString("fb_id", "");
            // 페이스북 이미지 https://graph.facebook.com/122841724958395/picture?type=large
            values.UserImageUrl = values.FacebookId == "" ? "image" : "https://graph.facebook.com/" + values.FacebookId + "/picture?type=large";
            values.UserEmail = mPref.getString("user_email", "Nothing Email");
            values.UserName = mPref.getString("user_name", "Nothing Name");
            // 네비에 사용자 정보 표시
            SetNaviInfo();
            // 만보기 서비스 시작
            StartStepService(true);
            // 지도 서비스 시작
            StartStoreService();
        } else {
            // 메인을 처음실행하는게 아니면
            // 프레프런스에 저장된 아이디를 values에 저장
            mPref = PreferenceManager.getDefaultSharedPreferences(this);
            values.UserId = mPref.getInt("user_id", -1);
            values.FacebookId = mPref.getString("fb_id", "");
            // 페이스북 이미지 https://graph.facebook.com/122841724958395/picture?type=large
            values.UserImageUrl = values.FacebookId == "" ? "image" : "https://graph.facebook.com/" + values.FacebookId + "/picture?type=large";
            values.UserEmail = mPref.getString("user_email", "Nothing Email");
            values.UserName = mPref.getString("user_name", "Nothing Name");
            // 네비에 사용자 정보 표시
            SetNaviInfo();
            if (loginType.equals(LOGIN_TYPE.First)) {
                // 처음 로그인하는 사용자이면
                // 만보기 서비스 종료 후 시작
                StopStepService();
                StartStepService(true);
                // 지도 서비스 시작
                StartStoreService();
            } else {
                // 로그인 한적이 있으면
                // 만보기 서비스 시작안했으면 시작
                StartStepService(false);
                // 지도 서비스 시작
                StartStoreService();
            }
        }
        // 프레프런스에 로그인상태 저장
        mPref.edit().putBoolean("START_FIRST", false).apply();
        mPref.edit().putBoolean("LOGIN_FIRST", false).apply();
        mLoginType = LOGIN_TYPE.Continue;
        mStartType = START_TYPE.Continue;
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
    private void StartStepService(boolean isLoadRecord) {
        // 서비스 시작
        if (isServiceRunningCheck() == false) {
            // 만보기
            m_intent = new Intent(MainActivity.this, StepBackgroundService.class);
            m_receiver_step = new MyMainLocalRecever();

            // 로그인하는 경우
            m_intent.putExtra("load_record", isLoadRecord);

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

    // 지도 서비스 권한 체크
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            ArrayList<StoreVO> storeVOs = null;
            StoreDB storeDB = null;
            // 위치 확인하여 위치 표시 시작
            startLocationService();
            // 위치 관리자 객체 참조
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            mPendingIntentList = new ArrayList();
            // 가맹점 위치 서비스 등록
            storeDB = new StoreDB(getApplicationContext());
            storeVOs = storeDB.SelectAllStore();
            for (StoreVO store : storeVOs) {
                register(store.StoreID, store.Y, store.X, 500, store.StoreName, store.URL, -1);
            }
            // 수신자 객체 생성하여 등록
            mIntentReceiverMap = new CoffeeIntentReceiver(m_mapIntentKey);
            registerReceiver(mIntentReceiverMap, mIntentReceiverMap.getFilter());
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).setGravity(Gravity.BOTTOM, 0, 30);
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    private void StartStoreService() {
        // 지도 일부 단말의 문제로 인해 초기화 코드 추가
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 지도 서비스 권한 체크
        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

    }

    // 플로팅버튼 보임/안보임
    private void setVisibleFab(boolean bVisible) {
        if (mFab != null) {
            mFab.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    // 플래그 화면 전환
    public void replaceFragment(Fragments frag) {
        Fragment fragment = null;
        setVisibleFab(true);
        switch (frag) {
            case Home: {
                fragment = new HomeFragment();
                break;
            }
            case MyPoint: {
                fragment = new MypointFragment();
                setVisibleFab(false);
                break;
            }
            case Map: {
                fragment = new MapFragment();
                setVisibleFab(false);
                break;
            }
            case Rank: {
                fragment = new RankFragment();
                setVisibleFab(false);
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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_fragment_main, fragment)
                .commit();

//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
////        transaction.add(R.id.content_fragment_main, fragment);
//        transaction.replace(R.id.content_fragment_main, fragment);
////        transaction.addToBackStack(frag.name());
//        transaction.commit();
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
        mViewUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                // 권한
                PermissionListener readExtraListener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        // 이미지 가져오기
                        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 카메라
                                doTakeCameraAction();
                            }
                        };

                        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 앨범
                                doTakeAlbumAction();
                            }
                        };

                        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 취소
                                dialog.dismiss();
                            }
                        };

                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Select Upload Image")
                                .setPositiveButton("Camera", cameraListener)
                                .setNeutralButton("Gallery", albumListener)
                                .setNegativeButton("Cancel", cancelListener)
                                .show();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Log.d("readExtraListener", "Permission Denied\n" + deniedPermissions.toString());
                    }
                };

                new TedPermission(v.getContext())
                        .setPermissionListener(readExtraListener)
                        .setDeniedMessage("If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();

            }
        });

    }

    private File createImageFile(String filename) throws IOException {
        // create an image file name
        String imagefilename = filename + "_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HealthFree/" + imagefilename);

        // Save a file path
        absolutePath = storageDir.getAbsolutePath();
        Log.i("createImageFile", storageDir.getAbsolutePath());
        return storageDir;
    }

    public void doTakeCameraAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File photoFile = null;
        try {
            photoFile = createImageFile("tmp");
        } catch (IOException ex) {

        }

        if (photoFile != null) {
            // getUriForFile의 두 번째 인자는 Manifest provier의 authorites와 일치해야함
            // photoURI : file://로 시작, FileProvider (Content Provider 하위)는 content://로 시작
            // 누가(7.0)이상부터는 file://로 시작되는 Uri의 값을 다른 앱과 주고받기
            String pkgname = this.getPackageName();
            mImageCaptureUri = FileProvider.getUriForFile(this, pkgname, photoFile);

            Log.i("photoFile", photoFile.toString());
            Log.i("mImageCaptureUri", mImageCaptureUri.toString());

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    public void doTakeAlbumAction() {
        //앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    // 네비뷰 사용자정보 데이터 표출
    private void SetNaviInfo() {
        // 네비뷰 > 상단뷰 > 사용자정보뷰에 로그인정보 입력
//        BitmapDrawable pDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.img_kongyu);
//        RoundedAvatarDrawable pRoundDrawable = new RoundedAvatarDrawable(pDrawable.getBitmap());
//        Glide.with(this).load(R.drawable.img_kongyu).into(mViewUserImage);
        String imgPath = mPref.getString("userImage", "image");

        if (values.FacebookId == "") {
            Glide.with(this).load(Uri.fromFile(new File(imgPath)))
                    .bitmapTransform(new CropCircleTransformation(this))
                    .placeholder(R.drawable.blank_profile)
                    .error(R.drawable.blank_profile)
                    .into(mViewUserImage);
        } else {
            Glide.with(this).load(values.UserImageUrl)
                    .bitmapTransform(new CropCircleTransformation(this))
                    .placeholder(R.drawable.blank_profile)
                    .error(R.drawable.blank_profile)
                    .into(mViewUserImage);
        }
//        mViewUserImage.setImageDrawable(pRoundDrawable);

        mViewUserEmail.setText(values.UserEmail);
        mViewUserName.setText(values.UserName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_receiver_step != null) {
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
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPressedTime;

            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                super.onBackPressed();
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            }
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
        } else if (id == R.id.nav_logout) {
            // 로그아웃
            LogOutPreExcute();
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivityForResult(mainIntent, 1004);
            Log.i("Main", "로그아웃");
            Log_value();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void LogOutPreExcute() {
        // 지도서비스 중지
        unregister_map();
        // 걸음수체크 서비스 중지
        StopStepService();
        // 사용자 정보 저장
        UpdateRecord();
        // values 초기화 (ID, EMAIL 다 초기화)
        values.Step = 0;
        values.Distance = 0;
        values.RunningSec = 0;
        values.Calorie = 0;
        values.UserId = -1;
        values.FacebookId = "";
        values.UserEmail = "Nothing Email";
        values.UserName = "Nothing Name";
        // 리레퍼런스 LOGIN_TYPE false로 변경
        if (mPref == null) mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPref.edit().putBoolean("LOGIN_FIRST", true).apply();
        LoginManager.getInstance().logOut(); // 페이스북 로그아웃
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

    /**
     * 등록한 정보 해제
     */
    private void unregister_map() {
        if (mPendingIntentList != null) {
            for (int i = 0; i < mPendingIntentList.size(); i++) {
                PendingIntent curIntent = (PendingIntent) mPendingIntentList.get(i);

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

        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        long minTime = 10000; // 통지사이의 최소 시간간격 (miliSecond) 1000ms = 1s
        float minDistance = 1; // 통지사이의 최소 변경거리 (m)

        try {
            // GPS 기반 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

/*            // 네트워크 기반 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);*/
        } catch (SecurityException ex) {
            Log.e("LocationService", ex.getMessage());
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
            Double latitude = location.getLatitude();   // 위도
            Double longitude = location.getLongitude(); // 경도
            float accuracy = location.getAccuracy();    // 정확도
            String provider = location.getProvider();   // 위치제공자 Gps, Network

            String msg = "Provider : " + provider + "\nLatitude : " + latitude + "\nLongitude : " + longitude + "\nAccuracy : " + accuracy;
            Log.d("GPSLocationService", msg);
//            mToastWalk.setText(msg);
//            mToastWalk.show();

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
//            showCurrentLocation(latitude, longitude);

            // 현재 위치를 이용해 LatLon 객체 생성
            LatLng newPoint = new LatLng(latitude, longitude);
            if (m_animateCamera) {
                old_latitude = latitude;
                old_longitude = longitude;

                // MapFragment mapF = (MapFragment) mMapFragment;
                MapFragment mapF = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.content_fragment_map);
                if (mapF != null)
                    mapF.showCurrentLocation(m_animateCamera, old_latitude, old_longitude, latitude, longitude);
            }

            Location locationS = new Location("point S");
            Location locationE = new Location("point E");
            locationS.setLatitude(Double.parseDouble(Double.toString(old_latitude)));
            locationS.setLongitude(Double.parseDouble(Double.toString(old_longitude)));
            locationE.setLatitude(Double.parseDouble(Double.toString(latitude)));
            locationE.setLongitude(Double.parseDouble(Double.toString(longitude)));
            double distance1 = locationS.distanceTo(locationE) / 1000.0; // m -> km
            double distance = Math.round(distance1 * 10.0) / 10.0; // 소수점 1자리 표시
            values.Distance = values.Distance + distance; //총 이동거리
            values.Distance = Math.round(values.Distance * 10.0) / 10.0; // 소수점 1자리 표시
            Log.d("Main", "onLocationChanged, distance: " + distance + ", total distance: " + values.Distance);

            old_latitude = latitude;
            old_longitude = longitude;
            m_animateCamera = false;
        }

        public void onProviderDisabled(String provider) {
            // Disabled 시
            Log.d("Location", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled 시
            Log.d("Location", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("Location", "onStatusChanged, provider:" + provider + ", status:" + status + ", Bundle:" + extras);
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
//                builder.setSmallIcon(R.mipmap.ic_launcher_main); // 메인아이콘으로 변경

                builder.setTicker("There is a partner of HeathFree around!"); //알림이 발생될 때 잠시 보이는 글씨
                //상태바를 드래그하여 아래로 내리면 보이는 알림창(확장 상태바)의 아이콘 모양 지정
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_main));

                Boolean isNoti_sound = mPref.getBoolean("notifications_new_message", false);
                String noti_sound = mPref.getString("notifications_new_message_ringtone", "");
                Boolean isVibration = mPref.getBoolean("notifications_new_message_vibrate", false);

                if(isNoti_sound) {
                    // 알림소리 설정
                    if(TextUtils.isEmpty(noti_sound)){
                        // 무음
                    }else {
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),Uri.parse(noti_sound));
                        if(ringtone == null){

                        }else {
                            builder.setSound(Uri.parse(noti_sound)); // 알림소리 설정
                        }
                    }

                    if(isVibration){
                        // 진동설정
                        builder.setVibrate(new long[]{1000, 1000});
                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    }else {
                        builder.setVibrate(new long[]{1000, 0});
                        builder.setDefaults(Notification.DEFAULT_SOUND);
                    }

                }else {
                    // 알림소리 off
                    builder.setDefaults(0);
                }

                builder.setContentTitle("There is  " + name + "  around.");    //알림창에서의 제목
                builder.setContentText("Touch it.");   //알림창에서의 글씨
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
