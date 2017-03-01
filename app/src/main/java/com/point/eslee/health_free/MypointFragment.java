package com.point.eslee.health_free;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.point.eslee.health_free.database.DataBases;
import com.point.eslee.health_free.database.DbOpenHelper;


public class MypointFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView mCurrentPointView;
    private TextView mTotalPointView;
    private View mRefreshView;
    private ScrollView mScrollView;

    private ListView mDetailListView;
    private ListViewPointAdapter mAdapter;

    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;

    public MypointFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MypointFragment newInstance(String param1, String param2) {
        MypointFragment fragment = new MypointFragment();
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
        View view = inflater.inflate(R.layout.fragment_mypoint, container, false);
        getActivity().setTitle(R.string.nav_menuname_myinfo);
        // Inflate the layout for this fragment

        mCurrentPointView = (TextView) view.findViewById(R.id.mypoint_current_point); // 현재 포인트
        mTotalPointView = (TextView) view.findViewById(R.id.mypoint_total_point); // 누적 포인트
        mRefreshView = (View) view.findViewById(R.id.mypoint_refresh_point); // 새로고침
        final Spinner spinner_search_option_year = (Spinner) view.findViewById(R.id.mypoint_search_option_year); // 검색옵션
        Spinner spinner_search_option_month = (Spinner) view.findViewById(R.id.mypoint_search_option_month); // 검색옵션
        mDetailListView = (ListView) view.findViewById(R.id.mypoint_details_listview); // 사용내역
        mScrollView = (ScrollView) view.findViewById(R.id.mypoint_scroll); // 스크롤뷰

        // 포인트 조회
        mCurrentPointView.setText(Common.get_commaString(1027));
        mTotalPointView.setText(Common.get_commaString(3520));

        // DB연결
        mDbOpenHelper = new DbOpenHelper(getActivity());
        mDbOpenHelper.open();

        // 리스트 뷰 초기화
        mAdapter = new ListViewPointAdapter();
        mDetailListView.setAdapter(mAdapter);
        mDetailListView.setClickable(false); // 클릭 여부

        mRefreshView.setOnClickListener(this); // 새로고침 클릭이벤트

        // 검색 옵션 선택 (월)
        spinner_search_option_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String year = String.valueOf(spinner_search_option_year.getSelectedItem());
                String month = String.valueOf(adapterView.getItemAtPosition(i));
//                Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_SHORT).show();
                setDetailList(year, month); // 리스트 뷰 표출
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mDetailListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mypoint_refresh_point:
                try {
                    // 포인트 새로고침
                    int ran = (int) (Math.random() * 1500) + 500;
                    int ran_t = (int) (Math.random() * 2000) + ran;

                    mCurrentPointView.setText(Common.get_commaString(ran));
                    mTotalPointView.setText(Common.get_commaString(ran_t));

                } catch (Exception ex) {

                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }


    private void setDetailList(String year, String month){
        int id = -1;
        String sTitle = "";
        String sUseDate = "";
        int sPoint = 0;
        String sUseDetail = "";

        mAdapter.clearItem();
        mCursor =  null;
        mCursor = mDbOpenHelper.getPointWhereDate(year,month);

        while (mCursor.moveToNext()){
            id = mCursor.getInt(mCursor.getColumnIndex(DataBases.PointTable._ID));
            sTitle = mCursor.getString(mCursor.getColumnIndex(DataBases.PointTable.USE_WHERE));
            sUseDate = mCursor.getString(mCursor.getColumnIndex(DataBases.PointTable.USE_DATE));
            sPoint = mCursor.getInt(mCursor.getColumnIndex(DataBases.PointTable.USE_POINT));
            sUseDetail = mCursor.getString(mCursor.getColumnIndex(DataBases.PointTable.USE_TYPE));

            mAdapter.addItem(id,sTitle,sUseDate,sPoint,sUseDetail);
        }
        mCursor.close();
        mAdapter.notifyDataSetChanged();

//        if(year.equals("2016")){
//            switch (month){
//                case "12":{
//                    mAdapter.addItem("Starbucks", "2016.12.31 13:03",-300,"Use");
//                    mAdapter.addItem("Walking","2016.12.27 19:00",500,"Save Walking");
//                    mAdapter.addItem("Walking","2016.12.24 19:00",450,"Save Walking");
//                    mAdapter.addItem("Starbucks", "2016.12.23 14:10",-500,"Use");
//                    mAdapter.addItem("Starbucks", "2016.12.17 17:03",-300,"Use");
//                    mAdapter.addItem("Weekly Event","2016.12.15 09:10",1000,"Event");
//                    mAdapter.addItem("Starbucks", "2016.12.15 10:43",-500,"Use");
//                    mAdapter.addItem("Walking","2016.12.10 19:00",500,"Save Walking");
//                    mAdapter.addItem("Walking","2016.12.03 19:00",450,"Save Walking");
//                    mAdapter.addItem("Burgerking","2016.12.01 12:20",-400,"Use");
//                    break;
//                }
//                case "11":{
//                    mAdapter.addItem("Weekly Event","2017.02.11 15:28",1000,"Event");
//                    mAdapter.addItem("Starbucks", "2017.02.05 10:43",-500,"Use");
//                    mAdapter.addItem("Walking","2017.02.04 19:00",500,"Save Walking");
//                    mAdapter.addItem("Walking","2017.02.03 19:00",450,"Save Walking");
//                    mAdapter.addItem("Burgerking","2017.02.01 12:20",-400,"Use");
//                    break;
//                }
//            }
//
//        }else{
//            // 2017
//            switch (month){
//                case "1":{
//                    mAdapter.addItem("Starbucks", "2017.01.23 14:10",-500,"Use");
//                    mAdapter.addItem("Starbucks", "2017.01.17 17:03",-300,"Use");
//                    mAdapter.addItem("Weekly Event","2017.01.15 09:10",1000,"Event");
//                    mAdapter.addItem("Starbucks", "2017.01.15 10:43",-500,"Use");
//                    mAdapter.addItem("Walking","2017.01.10 19:00",500,"Save Walking");
//                    mAdapter.addItem("Walking","2017.01.03 19:00",450,"Save Walking");
//                    mAdapter.addItem("Burgerking","2017.01.01 12:20",-400,"Use");
//                    break;
//                }
//                case "2":{
//                    mAdapter.addItem("Weekly Event","2017.02.11 15:28",1000,"Event");
//                    mAdapter.addItem("Starbucks", "2017.02.05 10:43",-500,"Use");
//                    mAdapter.addItem("Walking","2017.02.04 19:00",500,"Save Walking");
//                    mAdapter.addItem("Walking","2017.02.03 19:00",450,"Save Walking");
//                    mAdapter.addItem("Burgerking","2017.02.01 12:20",-400,"Use");
//                    break;
//                }
//            }
//
//        }

    }


}
