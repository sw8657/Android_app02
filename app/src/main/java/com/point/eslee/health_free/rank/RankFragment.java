package com.point.eslee.health_free.rank;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.point.eslee.health_free.R;
import com.point.eslee.health_free.VO.RankVO;
import com.point.eslee.health_free.database.RankDB;

import org.w3c.dom.Text;

public class RankFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView mPointTitle;
    private ListView mPointListView;
    private ListViewRankAdapter mPointAdapter;
    private RankDB mRankDB;

    private TextView mPointMyNum;
    private TextView mPointMyName;
    private TextView mPointMyValue;


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
        mPointTitle = (TextView) view.findViewById(R.id.rank_point_title); // 포인트 제목
        mPointListView = (ListView) view.findViewById(R.id.rank_point_listview); // 포인트 리스트뷰

        mPointMyNum = (TextView) view.findViewById(R.id.rank_point_mynum); // 내 포인트 순위
        mPointMyName = (TextView) view.findViewById(R.id.rank_point_myname); // 내 이름
        mPointMyValue = (TextView) view.findViewById(R.id.rank_point_myvalue); // 내 포인트값

        // 리스트뷰 초기화
        mPointAdapter = new ListViewRankAdapter();
        mPointListView.setAdapter(mPointAdapter);
        mPointListView.setClickable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);
        getActivity().setTitle("Rank");
        // Inflate the layout for this fragment

        // 레이아웃 초기화
        SetLayOut(view);

        //DB연결
        mRankDB = new RankDB(this.getContext());

        // TODO: 서버에서 랭킹 조회하기
        setRankPoint();
        setMyRankPoint();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rank_point_title:
                try {
                    // TODO: 포인트 랭킹 새로고침
                    setRankPoint();
                    setMyRankPoint();
                } catch (Exception ex) {

                }
        }
    }

    private void setRankPoint() {
        mPointAdapter.clearItem();
        try {
            mPointAdapter.addItemList(mRankDB.SelectRankPoint());
        } catch (Exception ex) {
            Log.e("RankFragment : ", ex.getMessage());
        }
        mPointAdapter.notifyDataSetChanged();
    }

    private void setMyRankPoint(){
        RankVO rank = null;
        mPointMyNum.setText("0");
        mPointMyName.setText("nothing user");
        mPointMyValue.setText("nothing value");
        try{
            rank = mRankDB.SelectMyRankPoint();
            mPointMyNum.setText(String.valueOf(rank.getNum()));
            mPointMyName.setText(rank.getTitle());
            mPointMyValue.setText(rank.getValue());
        }catch (Exception ex){
            Log.e("RankFragment : ",ex.getMessage());
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
