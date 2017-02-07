package com.point.eslee.health_free;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    private TextView mStepView;
    private TimerTask mTask;
    private Timer mTimer;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Home");
        // Inflate the layout for this fragment

        mStepView = (TextView) view.findViewById(R.id.home_step_value); // 걸음수 뷰
        mStepView.setText(Common.get_commaString(values.Step));

        // 타이머 가져오기
        mTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(getActivity(), "" + values.Step, Toast.LENGTH_SHORT);
                        mStepView.setText(Common.get_commaString(values.Step));
                    }
                });
            }
        };

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(mTask, 0, 500);

        return view;
    }

    @Override
    public void onDestroyView() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onDestroyView();
    }
}
