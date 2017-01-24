package com.point.eslee.health_free;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class StatisticsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LineChart mLineChart;
    private TextView mChartTitle;
    private TextView mChartPeriod;
    private TextView mChartStepMean;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
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
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("Statistics Fragment");
        // Inflate the layout for this fragment

        Button btnChartWeekly = (Button) view.findViewById(R.id.chart_weekly_button);
        Button btnChartMonthly = (Button) view.findViewById(R.id.chart_monthly_button);
        mLineChart = (LineChart) view.findViewById(R.id.chart);
        mChartTitle = (TextView) view.findViewById(R.id.chart_title);
        mChartPeriod = (TextView) view.findViewById(R.id.chart_period);
        mChartStepMean = (TextView) view.findViewById(R.id.chart_step_mean);

        // 버튼 이벤트 핸들러 생성
        btnChartWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"주간",Toast.LENGTH_SHORT).show();
                CreateChartView1();
            }
        });

        btnChartMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"월간",Toast.LENGTH_SHORT).show();
                CreateChartView2();
            }
        });

        btnChartWeekly.callOnClick(); // 주간 버튼 클릭

        return view;
    }

    private void CreateChartView(){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(6f, 2));
        entries.add(new Entry(2f, 3));
        entries.add(new Entry(18f, 4));
        entries.add(new Entry(9f, 5));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시


        mLineChart.setData(data);
        mLineChart.animateY(2000);

    }

    private void CreateChartView1(){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(3010f, 0));
        entries.add(new Entry(1405f, 1));
        entries.add(new Entry(4050f, 2));
        entries.add(new Entry(5240f, 3));
        entries.add(new Entry(3508f, 4));
        entries.add(new Entry(4344f, 5));
        entries.add(new Entry(3457f, 6));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("S");
        labels.add("M");
        labels.add("T");
        labels.add("W");
        labels.add("T");
        labels.add("F");
        labels.add("S");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(true); //값 표시

        mChartTitle.setText("1월 3주차");
        mChartPeriod.setText("01.16 - 01.22");
        mChartStepMean.setText(get_commaString(dataset.getAverage()));

        mLineChart.setData(data);
        mLineChart.animateY(2000);


    }

    private void CreateChartView2(){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(3233f, 0));
        entries.add(new Entry(2342f, 1));
        entries.add(new Entry(3122f, 2));
        entries.add(new Entry(2500f, 3));
        entries.add(new Entry(3012f, 4));
        entries.add(new Entry(1900f, 5));
        entries.add(new Entry(2003f, 6));
        entries.add(new Entry(3041f, 7));
        entries.add(new Entry(3000f, 8));
        entries.add(new Entry(5060f, 9));
        entries.add(new Entry(3010f, 10));
        entries.add(new Entry(3405f, 11));
        entries.add(new Entry(4040f, 12));
        entries.add(new Entry(3095f, 13));
        entries.add(new Entry(2838f, 14));
        entries.add(new Entry(2939f, 15));
        entries.add(new Entry(3064f, 16));
        entries.add(new Entry(3001f, 17));
//        entries.add(new Entry(0f, 18));
//        entries.add(new Entry(0f, 19));
//        entries.add(new Entry(0f, 20));
//        entries.add(new Entry(0f, 21));
//        entries.add(new Entry(0f, 22));
//        entries.add(new Entry(0f, 23));
//        entries.add(new Entry(0f, 24));
//        entries.add(new Entry(0f, 25));
//        entries.add(new Entry(0f, 26));
//        entries.add(new Entry(0f, 27));
//        entries.add(new Entry(0f, 28));
//        entries.add(new Entry(0f, 29));
//        entries.add(new Entry(0f, 30));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("1");
        labels.add("2");
        labels.add("3");
        labels.add("4");
        labels.add("5");
        labels.add("6");
        labels.add("7");
        labels.add("8");
        labels.add("9");
        labels.add("10");
        labels.add("11");
        labels.add("12");
        labels.add("13");
        labels.add("14");
        labels.add("15");
        labels.add("16");
        labels.add("17");
        labels.add("18");
        labels.add("19");
        labels.add("20");
        labels.add("21");
        labels.add("22");
        labels.add("23");
        labels.add("24");
        labels.add("25");
        labels.add("26");
        labels.add("27");
        labels.add("28");
        labels.add("29");
        labels.add("30");
        labels.add("31");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(false); // 값 표시
        dataset.setDrawCircleHole(false);
        dataset.setDrawCircles(false);

        mChartTitle.setText("1월");
        mChartPeriod.setText("01.01 - 01.31");
        mChartStepMean.setText(get_commaString(dataset.getAverage()));

        mLineChart.setData(data);
        mLineChart.animateY(2000);

    }

    /**
     * 숫자 -> 콤마 문자열 변환
     * @param num
     * @return
     */
    public static String get_commaString(float num){
        String result = String.valueOf(num);

        try {
            int intValue = Math.round(num);
            DecimalFormat df = new DecimalFormat("#,##0");
            result = df.format(intValue).toString();
        }catch (Exception ex){

        }


        return result;
    }


}
