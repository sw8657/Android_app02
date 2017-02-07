package com.point.eslee.health_free;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
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

    private TextView mCurrentPointView;
    private TextView mTotalPointView;
    private View mRefreshView;
    private ListView mDetailListView;
    private ArrayAdapter<String> mAdapter;

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
        getActivity().setTitle(R.string.nav_menuname_myinfo);
        // Inflate the layout for this fragment

        mCurrentPointView = (TextView) view.findViewById(R.id.myinfo_current_point); // 현재 포인트
        mTotalPointView = (TextView) view.findViewById(R.id.myinfo_total_point); // 누적 포인트
        mRefreshView = (View) view.findViewById(R.id.myinfo_refresh_point); // 새로고침
        Spinner spinner_search_option1 = (Spinner) view.findViewById(R.id.myinfo_search_option1); // 검색옵션
        Spinner spinner_search_option = (Spinner) view.findViewById(R.id.myinfo_search_option); // 검색옵션
        mDetailListView = (ListView) view.findViewById(R.id.myinfo_details_listview); // 사용내역

        // 포인트 조회
        mCurrentPointView.setText(Common.get_commaString(1027));
        mTotalPointView.setText(Common.get_commaString(3520));

        mRefreshView.setOnClickListener(this);

        // 검색 옵션
        spinner_search_option.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = String.valueOf(adapterView.getItemAtPosition(i));
//                Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String[] List_values = {"No Search results are currently selected."};
        mAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,List_values);
        mDetailListView.setAdapter(mAdapter);

        // 사용내역 선택
        mDetailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(getActivity().getApplicationContext(),adapterView.getItemAtPosition(i).toString(),Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.myinfo_refresh_point:
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


}
