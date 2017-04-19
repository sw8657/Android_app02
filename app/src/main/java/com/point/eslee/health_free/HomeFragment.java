package com.point.eslee.health_free;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.point.eslee.health_free.VO.RecordVO;
import com.point.eslee.health_free.database.RecordDB;

import java.util.Timer;
import java.util.TimerTask;


public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TimerTask mTask;
    private Timer mTimer;
    private RecordDB mRecordDB;

    private TextView mStepView;
    private TextView mDistanceView;
    private TextView mCalorieView;
    private TextView mTimeView;
    private TextView mSpeedView;

    public HomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        mStepView = (TextView) view.findViewById(R.id.home_step_value); // 걸음수 뷰
        mDistanceView = (TextView) view.findViewById(R.id.home_distance_value); // 거리 뷰
        mCalorieView = (TextView) view.findViewById(R.id.home_calorie_value); // 칼로리
        mTimeView = (TextView) view.findViewById(R.id.home_time_value); // 시간
        mSpeedView = (TextView) view.findViewById(R.id.home_speed_value); // 속도

        mStepView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetDataView();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Home");
        // Inflate the layout for this fragment

        // DB 연결
        mRecordDB = new RecordDB(this.getContext());

        // 레이아웃 초기화
        SetLayOut(view);
        SetDataView();
        SetStepViewTimer();

        return view;
    }

    @Override
    public void onDestroyView() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onDestroyView();
    }

    // 내 기록 데이터 조회하기
    private void SetDataView() {
        RecordVO record = null;
        int num_step = 0;
        double num_distance = 0; // Km
        double num_calorie = 0;
        double num_speed = 0; // Km/h
        int num_sec = 0; // sec
        String str_time = "00:00:00";

        try {
            record = mRecordDB.SelectLastRecord();

            num_step = record.getSteps() + values.Step;
            num_distance = record.getDistance() + values.Distance_sum;
            num_calorie = record.getCalorie() + values.Calorie;
            num_sec = record.getRunningTime() + Common.getRunningTimeSecond();
            str_time = Common.convertSecToTimeString(num_sec);
            num_speed = (num_distance / (double)num_sec) * 3600;

            mStepView.setText(Common.get_commaString(num_step));
            mDistanceView.setText(String.valueOf(num_distance));
            mCalorieView.setText(Common.get_commaString(num_calorie));
            mTimeView.setText(str_time);
            mSpeedView.setText(Common.get_commaString(num_speed));
        } catch (Exception ex) {
            Log.e("HomeFragment : ", ex.getMessage());
        }

    }

    // 타이머 초기화
    private void SetStepViewTimer() {
        // 타이머 가져오기
        mTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetDataView(); // 내 기록 데이터 조회하기
                    }
                });
            }
        };

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(mTask, 0, 500);
    }

}
