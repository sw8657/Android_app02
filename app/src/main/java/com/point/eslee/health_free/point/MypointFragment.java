package com.point.eslee.health_free.point;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.point.eslee.health_free.Common;
import com.point.eslee.health_free.R;
import com.point.eslee.health_free.database.DataBases;
import com.point.eslee.health_free.database.DbOpenHelper;
import com.point.eslee.health_free.database.MyPointDB;
import com.point.eslee.health_free.values;


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
    private Spinner mSpinner_search_option_year;
    private Spinner mSpinner_search_option_month;
    private View mRefreshView;
    private ScrollView mScrollView;
    private ListView mDetailListView;

    private ListViewPointAdapter mAdapter;
    private MyPointDB mMyPointDB;

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

    private void SetLayOut(View view) {
        // 레이아웃 초기화
        mCurrentPointView = (TextView) view.findViewById(R.id.mypoint_current_point); // 현재 포인트
        mTotalPointView = (TextView) view.findViewById(R.id.mypoint_total_point); // 누적 포인트
        mRefreshView = (View) view.findViewById(R.id.mypoint_refresh_point); // 새로고침
        mSpinner_search_option_year = (Spinner) view.findViewById(R.id.mypoint_search_option_year); // 검색옵션
        mSpinner_search_option_month = (Spinner) view.findViewById(R.id.mypoint_search_option_month); // 검색옵션
        mDetailListView = (ListView) view.findViewById(R.id.mypoint_details_listview); // 사용내역
        mScrollView = (ScrollView) view.findViewById(R.id.mypoint_scroll); // 스크롤뷰

        // 리스트 뷰 초기화
        mAdapter = new ListViewPointAdapter();
        mDetailListView.setAdapter(mAdapter);
        mDetailListView.setClickable(false); // 클릭 여부
        mRefreshView.setOnClickListener(this); // 새로고침 클릭이벤트

        mDetailListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypoint, container, false);
        getActivity().setTitle(R.string.nav_menuname_myinfo);
        // Inflate the layout for this fragment

        // 레이아웃 초기화
        SetLayOut(view);

        // DB연결
//        mMyPointDB = new MyPointDB(this.getContext());

        // 포인트 조회
//        int total_point = mMyPointDB.getTotalPointByUserId(values.UserId);
        int total_point = 200;
        mCurrentPointView.setText(Common.get_commaString(total_point));
        mTotalPointView.setText(Common.get_commaString(total_point + 2000));

        // 검색 옵션 선택 (월)
        mSpinner_search_option_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String year = String.valueOf(mSpinner_search_option_year.getSelectedItem());
                String month = String.valueOf(adapterView.getItemAtPosition(i));
//                Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_SHORT).show();
//                setDetailList(year, month); // 리스트 뷰 표출
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
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
                    int total_point = mMyPointDB.getTotalPointByUserId(values.UserId);
                    int ran_t = (int) (Math.random() * 2000) + total_point;

                    mCurrentPointView.setText(Common.get_commaString(total_point));
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

    private void setDetailList(String year, String month) {
        mAdapter.clearItem();
        try {
            mAdapter.addItemList(mMyPointDB.SelectPointWhereDate(year, month));
        } catch (Exception ex) {
            Log.e("MyPointError : ",ex.getMessage());
        }
        mAdapter.notifyDataSetChanged();
    }


}
