package com.miraens.eslee.test_layout1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MyinfoFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView mStepCountView;
    private TimerTask mTask;
    private Timer mTimer;

    public MyinfoFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MyinfoFragment newInstance(String param1, String param2) {
        MyinfoFragment fragment = new MyinfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_myinfo, container, false);
        getActivity().setTitle("Myinfo Fragment");

        Button btnReset = (Button) view.findViewById(R.id.myinfo_reset_button);
        mStepCountView = (TextView) view.findViewById(R.id.myinfo_step_count_view);
        mStepCountView.setText("" + values.Step);

        btnReset.setOnClickListener(this);

        // 타이머 가져오기
        mTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "" + values.Step, Toast.LENGTH_SHORT);
                        mStepCountView.setText("" + values.Step);
                    }
                });

            }
        };

        mTimer = new Timer();
//        mTimer.schedule(mTask, 1000, 3000);
        mTimer.scheduleAtFixedRate(mTask, 0, 3000);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.myinfo_reset_button:
                try {
                    Toast.makeText(getActivity(), "초기화", Toast.LENGTH_SHORT);
                    values.Step = 0;
                    mStepCountView.setText("" + values.Step);
                } catch (Exception ex) {

                }
        }
    }


    @Override
    public void onDestroyView() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onDestroyView();
    }


}
