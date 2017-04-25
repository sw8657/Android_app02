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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;


public class MapFragment extends Fragment {

    private GoogleMap m_googleMap;
    private MapView m_mapView;

    private static final String TAG = "MapFragment";

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
//        m_googleMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//        m_googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        m_mapView = (MapView) view.findViewById(R.id.mapView);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();
        // 일부 단말의 문제로 인해 초기화 코드 추가
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                m_googleMap = googleMap;

                //        checkDangerousPermissions(); //Main에서 수행
                m_googleMap.setMyLocationEnabled(true);

                // 지도 유형 설정. 지형도인 경우에는 GoogleMap.MAP_TYPE_TERRAIN, 위성 지도인 경우에는 GoogleMap.MAP_TYPE_SATELLITE
                m_googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5571895, 126.923642)).title("스타벅스 홍대역점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5568004, 126.9199674)).title("스타벅스 동교점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5532579, 126.9248262)).title("스타벅스 홍대갤러리점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5518991, 126.9232424)).title("스타벅스 홍대공원점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.558898, 126.9275124)).title("스타벅스 동교삼거리점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5529804, 126.9218637)).title("스타벅스 홍대로데오점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5501915, 126.9232343)).title("스타벅스 홍대삼거리점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5513451, 126.9169083)).title("스타벅스 서교점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5533397, 126.918578)).title("스타벅스 서교동사거리점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5523765, 126.9377746)).title("스타벅스 서강대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.55649, 126.9371201)).title("스타벅스 신촌점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5586535, 126.9366775)).title("스타벅스 연대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5587566, 126.9402234)).title("스타벅스 신촌기차역점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5577519, 126.9381461)).title("스타벅스 신촌명물거리점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5561264, 126.9392538)).title("스타벅스 신촌대로점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5584238, 126.9265576)).title("카페베네 동교동로터리점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5567823, 126.9199795)).title("카페베네 동교중앙점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5546741, 126.9218087)).title("카페베네 홍대역점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5592297, 126.9398063)).title("카페베네 신촌점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.557535, 126.9190876)).title("파리바게트 서교점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5557298, 126.9203644)).title("파리바게트 홍대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5516471, 126.9163089)).title("파리바게트 합정역점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5530505, 126.933188)).title("파리바게트 마포창천"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5585631, 126.9278075)).title("파리바게트"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5554523, 126.9233054)).title("커피빈 홍대역점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5585482, 126.9367567)).title("맥도날드 연세대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5556182, 126.937167)).title("맥도날드 신촌점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5550759, 126.9219723)).title("맥도날드 홍익대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5560626, 126.9097254)).title("맥도날드 망원점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5529559, 126.9238713)).title("롯데리아 홍대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.554606, 126.9356838)).title("롯데리아 신촌로터리점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5556735, 126.9235575)).title("버거킹 홍대역점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.555599, 126.9372851)).title("버거킹 신촌점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5559796, 126.9351688)).title("KFC 신촌점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5559796, 126.923115)).title("KFC 홍대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5556458, 126.936585)).title("투썸플레이스 신촌점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.5511761, 126.9392511)).title("투썸플레이스 서강대점"));
                m_googleMap.addMarker(new MarkerOptions().position(new LatLng(37.558814, 126.9419172)).title("투썸플레이스 신촌기차역점"));


                m_googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //Toast.makeText(getApplicationContext(), "마커클릭테스트", Toast.LENGTH_LONG).show();
                        // TODO Auto-generated method stub
                        return false;
                    }
                });

                m_googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
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
            }
        });


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
    public void onResume() {
        super.onResume();
        m_mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        m_mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }





    private View.OnClickListener onClickSearchButton = new View.OnClickListener() {

        @Override
        public void onClick(View view) {


            Toast.makeText(getContext(), "지도검색", Toast.LENGTH_SHORT).show();
        }
    };


    /**
     * 현재 위치의 지도를 보여주기 위해 정의한 메소드
     *
     * @param latitude
     * @param longitude
     */

    public void showCurrentLocation(boolean bAnimateCamera, Double old_latitude, Double old_longitude, Double latitude, Double longitude) {
        // 현재 위치를 이용해 LatLon 객체 생성
        LatLng newPoint = new LatLng(latitude, longitude);
        if (bAnimateCamera) {
            m_googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 15));
        }

        m_googleMap.addPolyline(new PolylineOptions().geodesic(true)
                .add(new LatLng(old_latitude, old_longitude))
                .add(new LatLng(latitude, longitude))
                .width(10)
                .color(Color.BLUE)
        );
    }


}
