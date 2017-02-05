package com.point.eslee.health_free;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class MapFragment extends Fragment {

    private GoogleMap m_map;
    private MapView m_mapView;

    private static final String TAG = "MapFragment";

    private LocationManager mLocationManager;
    private CoffeeIntentReceiver mIntentReceiver;
    ArrayList mPendingIntentList;
    String intentKey = "coffeeProximity";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MapFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        getActivity().setTitle("Map");
        // Inflate the layout for this fragment

        // 지도 객체 참조
//        m_map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        m_map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();

        // 일부 단말의 문제로 인해 초기화 코드 추가
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 위치 확인하여 위치 표시 시작
        startLocationService();
//        checkDangerousPermissions(); //Main에서 수행
        m_map.setMyLocationEnabled(true);
        // 지도 유형 설정. 지형도인 경우에는 GoogleMap.MAP_TYPE_TERRAIN, 위성 지도인 경우에는 GoogleMap.MAP_TYPE_SATELLITE
        m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5571895, 126.923642)).title("스타벅스 홍대역점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5568004, 126.9199674)).title("스타벅스 동교점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5532579, 126.9248262)).title("스타벅스 홍대갤러리점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5518991, 126.9232424)).title("스타벅스 홍대공원점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.558898, 126.9275124)).title("스타벅스 동교삼거리점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5529804, 126.9218637)).title("스타벅스 홍대로데오점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5501915, 126.9232343)).title("스타벅스 홍대삼거리점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5513451, 126.9169083)).title("스타벅스 서교점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5533397, 126.918578)).title("스타벅스 서교동사거리점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5523765, 126.9377746)).title("스타벅스 서강대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.55649, 126.9371201)).title("스타벅스 신촌점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5586535, 126.9366775)).title("스타벅스 연대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5587566, 126.9402234)).title("스타벅스 신촌기차역점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5577519, 126.9381461)).title("스타벅스 신촌명물거리점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5561264, 126.9392538)).title("스타벅스 신촌대로점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5584238, 126.9265576)).title("카페베네 동교동로터리점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5567823, 126.9199795)).title("카페베네 동교중앙점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5546741, 126.9218087)).title("카페베네 홍대역점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5592297, 126.9398063)).title("카페베네 신촌점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.557535, 126.9190876)).title("파리바게트 서교점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5557298, 126.9203644)).title("파리바게트 홍대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5516471, 126.9163089)).title("파리바게트 합정역점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5530505, 126.933188)).title("파리바게트 마포창천"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5585631, 126.9278075)).title("파리바게트"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5554523, 126.9233054)).title("커피빈 홍대역점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5585482, 126.9367567)).title("맥도날드 연세대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5556182, 126.937167)).title("맥도날드 신촌점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5550759, 126.9219723)).title("맥도날드 홍익대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5560626, 126.9097254)).title("맥도날드 망원점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5529559, 126.9238713)).title("롯데리아 홍대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.554606, 126.9356838)).title("롯데리아 신촌로터리점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5556735, 126.9235575)).title("버거킹 홍대역점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.555599, 126.9372851)).title("버거킹 신촌점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5559796, 126.9351688)).title("KFC 신촌점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5559796, 126.923115)).title("KFC 홍대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5556458, 126.936585)).title("투썸플레이스 신촌점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.5511761, 126.9392511)).title("투썸플레이스 서강대점"));
        m_map.addMarker(new MarkerOptions().position(new LatLng(37.558814, 126.9419172)).title("투썸플레이스 신촌기차역점"));

        // 위치 관리자 객체 참조
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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

        m_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Toast.makeText(getApplicationContext(), "마커클릭테스트", Toast.LENGTH_LONG).show();
                // TODO Auto-generated method stub
                return false;
            }
        });

        m_map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //Toast.makeText(getApplicationContext(), "(" + marker.getTitle() + ")", Toast.LENGTH_LONG).show();
                String Url = "";
                if (marker.getTitle().contains("스타벅스 홍대역점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9872";
                }
                if (marker.getTitle().contains("스타벅스 동교점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9837";
                }
                if (marker.getTitle().contains("스타벅스 홍대갤러리점")) {
                    Url = "http://www.istarbucks.co.kr";
                }
                if (marker.getTitle().contains("스타벅스 홍대공원점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9986";
                }
                if (marker.getTitle().contains("스타벅스 동교삼거리점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9888";
                }
                if (marker.getTitle().contains("스타벅스 홍대로데오점")) {
                    Url = "http://www.istarbucks.co.kr";
                }
                if (marker.getTitle().contains("스타벅스 홍대삼거리점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9602";
                }
                if (marker.getTitle().contains("스타벅스 서교점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=3056";
                }
                if (marker.getTitle().contains("스타벅스 서교동사거리점")) {
                    Url = "http://www.istarbucks.co.kr";
                }
                if (marker.getTitle().contains("스타벅스 서강대점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9983";
                }
                if (marker.getTitle().contains("스타벅스 신촌점")) {
                    Url = "http://www.istarbucks.co.kr";
                }
                if (marker.getTitle().contains("스타벅스 연대점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9639";
                }
                if (marker.getTitle().contains("스타벅스 신촌기차역점")) {
                    Url = "http://www.istarbucks.co.kr";
                }
                if (marker.getTitle().contains("스타벅스 신촌명물거리점")) {
                    Url = "http://www.istarbucks.co.kr/store/store_map.do?in_biz_cd=9530";
                }
                if (marker.getTitle().contains("스타벅스 신촌대로점")) {
                    Url = "http://www.istarbucks.co.kr";
                }
                if (marker.getTitle().contains("카페베네 동교동로터리점")) {
                    Url = "http://www.caffebene.co.kr";
                }
                if (marker.getTitle().contains("카페베네 동교중앙점")) {
                    Url = "http://www.caffebene.co.kr";
                }
                if (marker.getTitle().contains("카페베네 홍대역점")) {
                    Url = "http://www.caffebene.co.kr";
                }
                if (marker.getTitle().contains("카페베네 신촌점")) {
                    Url = "http://www.caffebene.co.kr";
                }
                if (marker.getTitle().contains("파리바게트 서교점")) {
                    Url = "http://www.paris.co.kr";
                }
                if (marker.getTitle().contains("파리바게트 홍대점")) {
                    Url = "http://www.paris.co.kr";
                }
                if (marker.getTitle().contains("파리바게트 합정역점")) {
                    Url = "http://www.paris.co.kr";
                }
                if (marker.getTitle().contains("파리바게트 마포창천")) {
                    Url = "http://www.paris.co.kr";
                }
                if (marker.getTitle().contains("파리바게트")) {
                    Url = "http://www.paris.co.kr";
                }
                if (marker.getTitle().contains("커피빈 홍대역점")) {
                    Url = "http://www.coffeebeankorea.com";
                }
                if (marker.getTitle().contains("맥도날드 연세대점")) {
                    Url = "http://www.mcdonalds.co.kr";
                }
                if (marker.getTitle().contains("맥도날드 신촌점")) {
                    Url = "http://www.mcdonalds.co.kr";
                }
                if (marker.getTitle().contains("맥도날드 홍익대점")) {
                    Url = "http://www.mcdonalds.co.kr";
                }
                if (marker.getTitle().contains("맥도날드 망원점")) {
                    Url = "http://www.mcdonalds.co.kr";
                }
                if (marker.getTitle().contains("롯데리아 홍대점")) {
                    Url = "http://www.lotteria.com";
                }
                if (marker.getTitle().contains("롯데리아 신촌로터리점")) {
                    Url = "http://www.lotteria.com";
                }
                if (marker.getTitle().contains("버거킹 홍대역점")) {
                    Url = "http://www.burgerking.co.kr";
                }
                if (marker.getTitle().contains("버거킹 신촌점")) {
                    Url = "http://www.burgerking.co.kr";
                }
                if (marker.getTitle().contains("KFC 신촌점")) {
                    Url = "http://www.kfckorea.com";
                }
                if (marker.getTitle().contains("KFC 홍대점")) {
                    Url = "http://www.kfckorea.com";
                }
                if (marker.getTitle().contains("투썸플레이스 신촌점")) {
                    Url = "https://www.twosome.co.kr";
                }
                if (marker.getTitle().contains("투썸플레이스 서강대점")) {
                    Url = "https://www.twosome.co.kr";
                }
                if (marker.getTitle().contains("투썸플레이스 신촌기차역점")) {
                    Url = "https://www.twosome.co.kr";
                }
                Intent internet = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
                startActivity(internet);
            }
        });

        // 수신자 객체 생성하여 등록
        mIntentReceiver = new CoffeeIntentReceiver(intentKey);
        getActivity().registerReceiver(mIntentReceiver, mIntentReceiver.getFilter());
        //Toast.makeText(getApplicationContext(), countTargets + "개 지점에 대한 근접 리스너 등록", Toast.LENGTH_LONG).show();

        /*Button stopBtn = (Button) findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                unregister();
                Toast.makeText(getApplicationContext(), "근접 리스너 해제", Toast.LENGTH_LONG).show();
            }
        });*/

        return view;
    }


    @Override
    public void onDestroyView() {
        unregister();

        super.onDestroyView();
    }

    /**
     * 등록한 정보 해제
     */
    private void unregister() {
        if (mPendingIntentList != null) {
            for (int i = 0; i < mPendingIntentList.size(); i++) {
                PendingIntent curIntent = (PendingIntent) mPendingIntentList.get(i);
                mLocationManager.removeProximityAlert(curIntent);
                mPendingIntentList.remove(i);
            }
        }

        if (mIntentReceiver != null) {
            getActivity().unregisterReceiver(mIntentReceiver);
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
        PendingIntent intent = PendingIntent.getBroadcast(getActivity(), id, proximityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mLocationManager.addProximityAlert(latitude, longitude, radius, expiration, intent);

        mPendingIntentList.add(intent);
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
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                mLastReceivedIntent = intent;

                int id = intent.getIntExtra("id", 0);
                String name = intent.getStringExtra("name");
                String url = intent.getStringExtra("url");
                double latitude = intent.getDoubleExtra("latitude", 0.0D);
                double longitude = intent.getDoubleExtra("longitude", 0.0D);

                //Toast.makeText(context, "근접한 마커 : " + name, Toast.LENGTH_LONG).show();


                //알림(Notification)을 관리하는 NotificationManager 얻어오기
                NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                //알림(Notification)을 만들어내는 Builder 객체 생성
                //API 11 버전 이하도 지원하기 위해 NotificationCampat 클래스 사용
                //만약 minimum SDK가 API 11 이상이면 Notification 클래스 사용 가능
                Notification.Builder builder = new Notification.Builder(getActivity());
                //Notification.Builder에게 Notification 제목, 내용, 이미지 등을 설정//////////////////////////////////////
                builder.setSmallIcon(android.R.drawable.ic_menu_myplaces);//상태표시줄에 보이는 아이콘 모양
                builder.setTicker("There is a partner of HeathFree around!"); //알림이 발생될 때 잠시 보이는 글씨
                //상태바를 드래그하여 아래로 내리면 보이는 알림창(확장 상태바)의 아이콘 모양 지정
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_myplaces));

                builder.setContentTitle("There is  " + name + "  around.");    //알림창에서의 제목
                builder.setContentText("Touch it.");   //알림창에서의 글씨

                Intent naver = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                PendingIntent pi = PendingIntent.getActivity(getActivity(), (int) System.currentTimeMillis(), naver, 0);
                builder.setContentIntent(pi);

                Notification notification = builder.build();   //Notification 객체 생성
                manager.notify(1, notification);             //NotificationManager가 알림(Notification)을 표시

            }
        }

        public Intent getLastReceivedIntent() {
            return mLastReceivedIntent;
        }

        public void clearReceivedIntents() {
            mLastReceivedIntent = null;
        }
    }


    private View.OnClickListener onClickSearchButton = new View.OnClickListener() {

        @Override
        public void onClick(View view) {


            Toast.makeText(getContext(), "지도검색", Toast.LENGTH_SHORT).show();
        }
    };


    /**
     * 현재 위치 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

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

    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(getActivity(), permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0])) {
                Toast.makeText(getActivity(), "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
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
            showCurrentLocation(latitude, longitude);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    /**
     * 현재 위치의 지도를 보여주기 위해 정의한 메소드
     *
     * @param latitude
     * @param longitude
     */

    double old_latitude;
    double old_longitude;
    int i = 0;

    private void showCurrentLocation(Double latitude, Double longitude) {
        // 현재 위치를 이용해 LatLon 객체 생성
        LatLng newPoint = new LatLng(latitude, longitude);
        if (i == 0) {
            old_latitude = latitude;
            old_longitude = longitude;
            m_map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 15));
        }

        m_map.addPolyline(new PolylineOptions().geodesic(true)
                .add(new LatLng(old_latitude, old_longitude))
                .add(new LatLng(latitude, longitude))
                .width(10)
                .color(Color.BLUE)
        );
        old_latitude = latitude;
        old_longitude = longitude;
        i = 1;
    }

}
