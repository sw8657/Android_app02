package com.point.eslee.health_free.rank;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.point.eslee.health_free.R;
import com.point.eslee.health_free.VO.RankVO;
import com.point.eslee.health_free.database.MyPointDB;
import com.point.eslee.health_free.database.RankDB;
import com.point.eslee.health_free.values;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class RankFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView mListView;
    private ListViewRankAdapter mAdapter;

    private TextView mMyNum;
    private TextView mMyName;
    private ImageView mMyImg;
    private TextView mMyValue;
    private Spinner mMenuSpinner;
    private SwipeRefreshLayout mSwipeRefresh;
    private ScrollView mScrollView;

    public RankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RankFragment newInstance(String param1, String param2) {
        RankFragment fragment = new RankFragment();
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
        mMenuSpinner = (Spinner) view.findViewById(R.id.rank_search_menu); // 메뉴 선택
        mListView = (ListView) view.findViewById(R.id.rank_listview); // 포인트 리스트뷰
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.rank_swipe_listview); // 당겨서 새로고침
        mScrollView = (ScrollView) view.findViewById(R.id.rank_scroll); // 스크롤뷰

        mMyNum = (TextView) view.findViewById(R.id.rank_my_num); // 내 포인트 순위
        mMyImg = (ImageView) view.findViewById(R.id.rank_my_img); // 프로필이미지
        mMyName = (TextView) view.findViewById(R.id.rank_my_name); // 내 이름
        mMyValue = (TextView) view.findViewById(R.id.rank_myvalue); // 내 포인트값

        // 서버에 랭킹정보 업데이트

        // 리스트뷰 초기화
        mAdapter = new ListViewRankAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setClickable(false);

        mMenuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                new UpdateUserRankInfo().execute((Void) null);
                // 서버에서 랭킹 조회하기
                new SelectRankServer().execute((Void) null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (mListView == null || mListView.getChildCount() == 0) ? 0 : mListView.getChildAt(0).getTop();
                mSwipeRefresh.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UpdateUserRankInfo().execute((Void) null);
                // 새로고침
                new SelectRankServer().execute((Void) null);

                // 새로고침 완료
                mSwipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Refresh", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);
        getActivity().setTitle("Ranking");
        // Inflate the layout for this fragment

        // 레이아웃 초기화
        SetLayOut(view);


        return view;
    }

    public class UpdateUserRankInfo extends AsyncTask<Void, Void, Boolean> {
        Integer step, Calorie, r_time, t_point;
        Double distance;
        Boolean iReady = false;

        @Override
        protected void onPreExecute() {
            try {
                step = values.Step;
                Calorie = values.Calorie;
                r_time = values.RunningSec;
                distance = values.Distance;
                t_point = new MyPointDB(getContext()).SelectTotalPoint();
                iReady = true;
            } catch (Exception ex) {

            }

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (iReady) {
                new RankDB(getContext()).UpdateRankInfo(step, distance, Calorie, t_point, r_time);
            }
            return null;
        }
    }


    public class SelectRankServer extends AsyncTask<Void, Void, Boolean> {
        RankVO myRank = null;
        ArrayList<RankVO> rankVOs = null;
        String sMenu = null;

        @Override
        protected void onPreExecute() {
            mAdapter.clearItem();
            try {
                sMenu = String.valueOf(mMenuSpinner.getSelectedItem());
                sMenu = values.FacebookId == "" ? sMenu + "Total" : sMenu;
            } catch (Exception ex) {
                Log.e("setRank", ex.getMessage());
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = false;
            try {
                myRank = new RankVO(0, -1, 1, values.UserImageUrl, values.UserName, "nothing value");
                rankVOs = new RankDB(getContext()).SelectRank(sMenu);

                for (int i = 0; i < rankVOs.size(); i++) {
                    if (rankVOs.get(i).getUserId() == values.UserId) {
                        myRank = rankVOs.get(i);
                        break;
                    }
                }

                result = true;
            } catch (Exception ex) {
                Log.e("setRank", ex.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                mAdapter.clearItem();

                // 내 순위 입력
                try {
                    mMyNum.setText(String.valueOf(myRank.getNum()));
                    Glide.with(getContext()).load(myRank.getImgUrl())
                            .bitmapTransform(new CropCircleTransformation(getContext()))
                            .placeholder(R.drawable.blank_profile)
                            .error(R.drawable.blank_profile)
                            .into(mMyImg);

                    mMyName.setText(myRank.getTitle());
                    mMyValue.setText(myRank.getValue());
                    // 친구 순위 입력

                } catch (Exception ex) {

                }

                try {
                    mAdapter.addItemList(rankVOs);
                } catch (Exception ex) {

                }
            }
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(aBoolean);
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


}
