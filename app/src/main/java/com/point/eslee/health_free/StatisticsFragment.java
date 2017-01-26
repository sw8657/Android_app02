package com.point.eslee.health_free;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.point.eslee.health_free.R.color.colorAccent;


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

    TextView mViewWeekly;
    TextView mViewMonthly;
    int mDataIndex = 0;

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
        CreateChartData_Week();
        CreateChartData_Month();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("Statistics");
        // Inflate the layout for this fragment

        mViewWeekly = (TextView) view.findViewById(R.id.chart_weekly_textview);
        mViewMonthly = (TextView) view.findViewById(R.id.chart_monthly_textview);
        mViewWeekly.setTag(0);
        mViewMonthly.setTag(0);

        FrameLayout viewChartArrowLeft = (FrameLayout) view.findViewById(R.id.chart_arrow_left);
        FrameLayout viewChartArrowRight = (FrameLayout) view.findViewById(R.id.chart_arrow_right);
        mLineChart = (LineChart) view.findViewById(R.id.chart);
        mChartTitle = (TextView) view.findViewById(R.id.chart_title);
        mChartPeriod = (TextView) view.findViewById(R.id.chart_period);
        mChartStepMean = (TextView) view.findViewById(R.id.chart_step_mean);

        // 버튼 이벤트 핸들러 생성
        mViewWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 버튼 색깔
                mViewWeekly.setTextColor(getResources().getColor(R.color.colorAccent));
                mViewWeekly.setTag(1);
                mViewMonthly.setTextColor(getResources().getColor(R.color.colorDarkGray));
                mViewMonthly.setTag(0);
                //Toast.makeText(getActivity(), "주간", Toast.LENGTH_SHORT).show();
                try {
                    mDataIndex = mDataWeek.size() - 1;
                    CreateChartView_Week_1(mDataIndex);
                } catch (Exception e) {
                }
            }
        });

        mViewMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 버튼 색깔
                mViewWeekly.setTextColor(getResources().getColor(R.color.colorDarkGray));
                mViewWeekly.setTag(0);
                mViewMonthly.setTextColor(getResources().getColor(R.color.colorAccent));
                mViewMonthly.setTag(1);
                //Toast.makeText(getActivity(), "월간", Toast.LENGTH_SHORT).show();
                try {
                    mDataIndex = mDataMonth.size() - 1;
                    CreateChartView_Month_1(mDataIndex);
                } catch (Exception e) {
                }
            }
        });

        viewChartArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int) mViewWeekly.getTag() == 1) {
                    // 주간
                    if (mDataIndex > 0) {
                        CreateChartView_Week_1(mDataIndex - 1);
                        mDataIndex--;
                    }
                } else {
                    // 월간
                    if(mDataIndex > 0){
                        CreateChartView_Month_1(mDataIndex - 1);
                        mDataIndex--;
                    }
                }
            }
        });

        viewChartArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int) mViewWeekly.getTag() == 1) {
                    // 주간
                    if (mDataIndex < (mDataWeek.size() - 1)) {
                        CreateChartView_Week_1(mDataIndex + 1);
                        mDataIndex++;
                    }
                } else {
                    // 월간
                    if(mDataIndex < (mDataMonth.size() -1)){
                        CreateChartView_Month_1(mDataIndex + 1);
                        mDataIndex++;
                    }
                }
            }
        });

        // 차트 디자인 옵션
        mLineChart.getLegend().setEnabled(false); // 범례
        mLineChart.setDescription(""); // 설명
        mLineChart.getAxisLeft().setDrawAxisLine(true); // 좌측 라인
        mLineChart.getAxisRight().setDrawAxisLine(false); // 우측 라인
        mLineChart.getAxisRight().setDrawGridLines(false); // 우측 그리드 라인
        mLineChart.getAxisLeft().setDrawGridLines(false); // 좌측 그리드 라인
        mLineChart.getAxisRight().setDrawLabels(false); // 우측 Y축 값표시
        mLineChart.getAxisLeft().setDrawLabels(true); // 좌측 Y축 값표시
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // X축 값 밑으로
        mLineChart.getXAxis().setDrawGridLines(false); // X축 그리드라인
        mLineChart.setDrawGridBackground(false); // 배경
        mLineChart.setDoubleTapToZoomEnabled(false); // 더블탭 줌인
        mLineChart.setPinchZoom(false); // 피치 줌인
        mLineChart.setScaleEnabled(false); // 축척 사용
        mLineChart.setHighlightPerTapEnabled(true);

        mViewWeekly.callOnClick(); // 주간 버튼 클릭

        return view;
    }

    class ChartInfo {
        public boolean using = false;
        public int DataIndex = 0;
    }

    List<LineDataSet> mDataWeek;
    List<LineDataSet> mDataMonth;

    private void CreateChartData_Week() {
        if (mDataWeek != null) {
            mDataWeek.clear();
        } else {
            mDataWeek = new ArrayList<LineDataSet>();
        }

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(3010f, 0));
        entries.add(new Entry(1405f, 1));
        entries.add(new Entry(4050f, 2));
        entries.add(new Entry(5240f, 3));
        entries.add(new Entry(3508f, 4));
        entries.add(new Entry(4344f, 5));
        entries.add(new Entry(3457f, 6));
        LineDataSet dataset = new LineDataSet(entries, "# of Calls");
        mDataWeek.add(dataset);

        ArrayList<Entry> entries1 = new ArrayList<>();
        entries1.add(new Entry(3010f, 0));
        entries1.add(new Entry(3040f, 1));
        entries1.add(new Entry(2030f, 2));
        entries1.add(new Entry(5063f, 3));
        entries1.add(new Entry(4230f, 4));
        entries1.add(new Entry(3256f, 5));
        entries1.add(new Entry(2030f, 6));
        LineDataSet dataset1 = new LineDataSet(entries1, "# of Calls");
        mDataWeek.add(dataset1);

        ArrayList<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(3948f, 0));
        entries2.add(new Entry(3183f, 1));
        entries2.add(new Entry(2957f, 2));
        entries2.add(new Entry(9694f, 3));
        entries2.add(new Entry(3819f, 4));
        entries2.add(new Entry(1273f, 5));
        entries2.add(new Entry(5348f, 6));
        LineDataSet dataset2 = new LineDataSet(entries2, "# of Calls");
        mDataWeek.add(dataset2);
    }

    private void CreateChartData_Month() {
        if (mDataMonth != null) {
            mDataMonth.clear();
        } else {
            mDataMonth = new ArrayList<LineDataSet>();
        }

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(3010f, 0));
        entries.add(new Entry(1403f, 1));
        entries.add(new Entry(3040f, 2));
        entries.add(new Entry(2030f, 3));
        entries.add(new Entry(5063f, 4));
        entries.add(new Entry(4230f, 5));
        entries.add(new Entry(3256f, 6));
        entries.add(new Entry(3010f, 7));
        entries.add(new Entry(3040f, 8));
        entries.add(new Entry(2030f, 9));
        entries.add(new Entry(5063f, 10));
        entries.add(new Entry(4230f, 11));
        entries.add(new Entry(3256f, 12));
        entries.add(new Entry(2030f, 13));
        entries.add(new Entry(3531f, 14));
        entries.add(new Entry(3577f, 15));
        entries.add(new Entry(3622f, 16));
        entries.add(new Entry(3667f, 17));
        entries.add(new Entry(3712f, 18));
        entries.add(new Entry(3758f, 19));
        entries.add(new Entry(3803f, 20));
        entries.add(new Entry(3848f, 21));
        entries.add(new Entry(3894f, 22));
        entries.add(new Entry(3939f, 23));
        entries.add(new Entry(3984f, 24));
        entries.add(new Entry(4029f, 25));
        entries.add(new Entry(4075f, 26));
        entries.add(new Entry(4120f, 27));
        entries.add(new Entry(4165f, 28));
        entries.add(new Entry(4211f, 29));
        entries.add(new Entry(3939f, 30));
        LineDataSet dataset = new LineDataSet(entries, "# of Calls");
        mDataMonth.add(dataset);

        ArrayList<Entry> entries1 = new ArrayList<>();
        entries1.add(new Entry(3010f, 0));
        entries1.add(new Entry(3040f, 1));
        entries1.add(new Entry(2030f, 2));
        entries1.add(new Entry(5063f, 3));
        entries1.add(new Entry(4230f, 4));
        entries1.add(new Entry(3256f, 5));
        entries1.add(new Entry(2030f, 6));
        entries1.add(new Entry(3010f, 7));
        entries1.add(new Entry(1403f, 8));
        entries1.add(new Entry(3040f, 9));
        entries1.add(new Entry(2030f, 10));
        entries1.add(new Entry(3040f, 11));
        entries1.add(new Entry(2030f, 12));
        entries1.add(new Entry(5063f, 13));
        entries1.add(new Entry(4230f, 14));
        entries1.add(new Entry(3256f, 15));
        entries1.add(new Entry(2030f, 16));
        entries1.add(new Entry(3010f, 17));
        entries1.add(new Entry(2790f, 18));
        entries1.add(new Entry(2749f, 19));
        entries1.add(new Entry(2709f, 20));
        entries1.add(new Entry(2668f, 21));
        entries1.add(new Entry(2627f, 22));
        entries1.add(new Entry(2587f, 23));
        entries1.add(new Entry(2546f, 24));
        entries1.add(new Entry(2506f, 25));
        entries1.add(new Entry(2465f, 26));
        entries1.add(new Entry(2425f, 27));
        entries1.add(new Entry(2384f, 28));
        entries1.add(new Entry(2344f, 29));
        entries1.add(new Entry(2587f, 30));
        LineDataSet dataset1 = new LineDataSet(entries1, "# of Calls");
        mDataMonth.add(dataset1);

        ArrayList<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(3948f, 0));
        entries2.add(new Entry(3183f, 1));
        entries2.add(new Entry(2957f, 2));
        entries2.add(new Entry(9694f, 3));
        entries2.add(new Entry(3819f, 4));
        entries2.add(new Entry(1273f, 5));
        entries2.add(new Entry(5348f, 6));
        entries2.add(new Entry(3256f, 7));
        entries2.add(new Entry(3010f, 8));
        entries2.add(new Entry(3040f, 9));
        entries2.add(new Entry(2030f, 10));
        entries2.add(new Entry(5063f, 11));
        entries2.add(new Entry(3819f, 12));
        entries2.add(new Entry(1273f, 13));
        entries2.add(new Entry(5348f, 14));
        entries2.add(new Entry(3256f, 15));
        entries2.add(new Entry(3294f, 16));
        entries2.add(new Entry(3248f, 17));
        entries2.add(new Entry(3203f, 18));
        entries2.add(new Entry(3157f, 19));
        entries2.add(new Entry(3112f, 20));
        entries2.add(new Entry(3067f, 21));
        entries2.add(new Entry(3021f, 22));
        entries2.add(new Entry(2976f, 23));
        entries2.add(new Entry(2930f, 24));
        entries2.add(new Entry(2885f, 25));
        entries2.add(new Entry(2839f, 26));
        entries2.add(new Entry(2794f, 27));
        entries2.add(new Entry(2748f, 28));
        entries2.add(new Entry(2703f, 29));
        entries2.add(new Entry(2976f, 30));
        LineDataSet dataset2 = new LineDataSet(entries2, "# of Calls");
        mDataMonth.add(dataset2);
    }

    String[] mDataWeekTitle = {"1월 2주차", "1월 3주차", "1월 4주차"};
    String[] mDataWeekTitle2 = {"01.09 - 01.15", "01.16 - 01.22", "01.23 - 01.29"};

    private void CreateChartView_Week_1(int week_num) {
        if (mDataWeek == null || mDataWeek.isEmpty()) {
            CreateChartData_Week();
        }
        LineDataSet dataset = mDataWeek.get(week_num);

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
        dataset.setDrawHighlightIndicators(true);
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(true); //값 표시

        mChartTitle.setText(mDataWeekTitle[week_num]);
        mChartPeriod.setText(mDataWeekTitle2[week_num]);
        mChartStepMean.setText(get_commaString(dataset.getAverage()));

        mLineChart.setData(data);
        mLineChart.animateY(2000);
    }

    String[] mDataMonthTitle = {"11월", "12월", "1월"};
    String[] mDataMonthTitle2 = {"11.01 - 11.30", "12.01 - 12.31", "01.01 - 01.31"};

    private void CreateChartView_Month_1(int month_num) {
        if (mDataMonth == null || mDataMonth.isEmpty()) {
            CreateChartData_Month();
        }
        LineDataSet dataset = mDataMonth.get(month_num);

        ArrayList<String> labels = new ArrayList<String>();
        for (int i = 0; i < dataset.getEntryCount(); i++) {
            labels.add(String.valueOf(i + 1));
        }

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(false); // 값 표시
        dataset.setDrawCircleHole(false);
        dataset.setDrawCircles(false);

        mChartTitle.setText(mDataMonthTitle[month_num]);
        mChartPeriod.setText(mDataMonthTitle2[month_num]);
        mChartStepMean.setText(get_commaString(dataset.getAverage()));

        mLineChart.setData(data);
        mLineChart.animateY(2000);
    }

    private void CreateChartView_Week_2() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(3010f, 0));
        entries.add(new Entry(2405f, 1));
        entries.add(new Entry(3050f, 2));
        entries.add(new Entry(4240f, 3));
        entries.add(new Entry(4508f, 4));
        entries.add(new Entry(6344f, 5));
        entries.add(new Entry(4457f, 6));

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
        dataset.setDrawHighlightIndicators(true);
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(true); //값 표시

        mChartTitle.setText("1월 4주차");
        mChartPeriod.setText("01.23 - 01.29");
        mChartStepMean.setText(get_commaString(dataset.getAverage()));

        mLineChart.setData(data);
        mLineChart.animateY(2000);
    }

    private void CreateChartView2() {
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
     *
     * @param num
     * @return
     */
    public static String get_commaString(float num) {
        String result = String.valueOf(num);

        try {
            int intValue = Math.round(num);
            DecimalFormat df = new DecimalFormat("#,##0");
            result = df.format(intValue).toString();
        } catch (Exception ex) {

        }


        return result;
    }


}
