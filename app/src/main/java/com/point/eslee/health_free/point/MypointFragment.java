package com.point.eslee.health_free.point;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.point.eslee.health_free.Common;
import com.point.eslee.health_free.R;
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
    private SwipeRefreshLayout mSwipeRefresh;

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
        //mTotalPointView = (TextView) view.findViewById(R.id.mypoint_total_point); // 누적 포인트
        mRefreshView = (View) view.findViewById(R.id.mypoint_refresh_point); // 새로고침
        mSpinner_search_option_year = (Spinner) view.findViewById(R.id.mypoint_search_option_year); // 검색옵션
        mSpinner_search_option_month = (Spinner) view.findViewById(R.id.mypoint_search_option_month); // 검색옵션
        mDetailListView = (ListView) view.findViewById(R.id.mypoint_details_listview); // 사용내역
        mScrollView = (ScrollView) view.findViewById(R.id.mypoint_scroll); // 스크롤뷰
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.mypoint_swipe_refresh); // 당겨서 새로고침

        // 이번달 선택하기
        String strYear = Common.getStringCurrentDate().split("-")[0];
        String strMonth = Common.getStringCurrentDate().split("-")[1];
        Integer iMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);

        if(strYear.equals("2017")){
            mSpinner_search_option_year.setSelection(0);
        }else {
            mSpinner_search_option_year.setSelection(1);
        }
        mSpinner_search_option_month.setSelection(iMonth);

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

        // 포인트 조회
        int total_point = new MyPointDB(getContext()).SelectTotalPoint();
        mCurrentPointView.setText(Common.get_commaString(total_point));

        // 검색 옵션 선택 (월)
        mSpinner_search_option_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String year = String.valueOf(mSpinner_search_option_year.getSelectedItem());
                String month = String.valueOf(adapterView.getItemAtPosition(i));
//                Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_SHORT).show();
                setDetailList(year, month); // 리스트 뷰 표출
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mDetailListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (mDetailListView == null || mDetailListView.getChildCount() == 0) ? 0 : mDetailListView.getChildAt(0).getTop();
                mSwipeRefresh.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 리스트뷰 새로고침
                int total_point = new MyPointDB(getContext()).SelectTotalPoint();
                mCurrentPointView.setText(Common.get_commaString(total_point));

                String year = String.valueOf(mSpinner_search_option_year.getSelectedItem());
                String month = String.valueOf(mSpinner_search_option_month.getSelectedItem());
                setDetailList(year, month); // 리스트 뷰 표출

                // 새로고침 완료
                mSwipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(),"Refresh",Toast.LENGTH_SHORT).show();
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
                    int total_point = new MyPointDB(getContext()).SelectTotalPoint();
                    mCurrentPointView.setText(Common.get_commaString(total_point));
                    //mTotalPointView.setText(Common.get_commaString(ran_t));
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
            mAdapter.addItemList(new MyPointDB(getContext()).SelectPointWhereDate(year, month));
        } catch (Exception ex) {
            Log.e("MyPointError : ", ex.getMessage());
        }
        mAdapter.notifyDataSetChanged();
    }


}
