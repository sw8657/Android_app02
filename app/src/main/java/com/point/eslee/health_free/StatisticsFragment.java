package com.point.eslee.health_free;

import android.database.Cursor;
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
import com.point.eslee.health_free.database.DbOpenHelper;
import com.point.eslee.health_free.database.RecordDB;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    String mDateString = "";
    RecordDB mRecordDB;

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

        // DB 연결
        mRecordDB = new RecordDB(this.getContext());

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
                    // 오늘날짜 기준
                    mDateString = getStringCurrentDate();
                    ViewData_TodayWeek();
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
                    mDateString = getStringCurrentDate();
//                    CreateChartView_Month_1(1);
                } catch (Exception e) {
                }
            }
        });

        viewChartArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int) mViewWeekly.getTag() == 1) {
                    // 주간
                    mDateString = getStringCalWeekDate(mDateString,false);
                    ViewData_TodayWeek();
                } else {
                    // 월간
//                    if (mDataIndex > 0) {
//                        CreateChartView_Month_1(mDataIndex - 1);
//                        mDataIndex--;
//                    }
                }
            }
        });

        viewChartArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int) mViewWeekly.getTag() == 1) {
                    // 주간
                    mDateString = getStringCalWeekDate(mDateString,true);
                    ViewData_TodayWeek();
                } else {
                    // 월간
//                    if (mDataIndex < (mDataMonth.size() - 1)) {
//                        CreateChartView_Month_1(mDataIndex + 1);
//                        mDataIndex++;
//                    }
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

    // 레이아웃 초기화
    private void SetLayOut() {

    }

    // 해당 날짜 주간(일-토) 통계 표출
    private void ViewData_TodayWeek() {
        ArrayList<Entry> entries = null;
        LineDataSet dataset = null;
        LineData data = null;
        ArrayList<String> labels = new ArrayList<String>();
        String sTitle = "";
        String sSubTitle = "";
        String[] sDates = null;

        // 초기값 입력
        labels.add("S");
        labels.add("M");
        labels.add("T");
        labels.add("W");
        labels.add("T");
        labels.add("F");
        labels.add("S");

        // DB 읽기
        // 이번달 몇째주 표출
        sTitle = getStringWeekNumberInMonth(mDateString);
        // 오늘날짜 기준 이번주 날짜 표출
        sDates = mRecordDB.SelectFirstAndEndDate(mDateString);
        sSubTitle = sDates[0] + " - " + sDates[1];
        // 오늘날짜 입력시 이번주 (일-토) 통계 표출
        entries = mRecordDB.SelectStatStepWeek(mDateString);
        dataset = new LineDataSet(entries, "# of Calls");
        data = new LineData(labels, dataset);

        // 차트 옵션
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawHighlightIndicators(true);
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(true); //값 표시

        // 값 표출
        mChartTitle.setText(sTitle);
        mChartPeriod.setText(sSubTitle);
        mChartStepMean.setText(Common.get_commaString(dataset.getAverage()));

        mLineChart.setData(data);
        mLineChart.animateY(2000);
    }

    private void ViewData_TodayMonth() {
        ArrayList<Entry> entries = null;
        LineDataSet dataset = null;
        LineData data = null;
        ArrayList<String> labels = new ArrayList<String>();
        String sTitle = "";
        String sSubTitle = "";
        String[] sDates = null;

        // DB 읽기
        // 이번달 몇째주 표출
        sTitle = getStringMonth(mDateString);
        // 오늘날짜 기준 이번주 날짜 표출
        sDates = mRecordDB.SelectFirstAndEndDate(mDateString);
        sSubTitle = sDates[0] + " - " + sDates[1];
        // 오늘날짜 입력시 이번주 (일-토) 통계 표출
        entries = mRecordDB.SelectStatStepWeek(mDateString);
        dataset = new LineDataSet(entries, "# of Calls");
        data = new LineData(labels, dataset);

        // X 축 입력
        for (int i = 0; i < dataset.getEntryCount(); i++) {
            labels.add(String.valueOf(i + 1));
        }

        // 차트 옵션
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true); //커브
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(false); // 값 표시
        dataset.setDrawCircleHole(false);
        dataset.setDrawCircles(false);

        // 값 표출
        mChartTitle.setText(sTitle);
        mChartPeriod.setText(sSubTitle);
        mChartStepMean.setText(Common.get_commaString(dataset.getAverage()));

        mLineChart.setData(data);
        mLineChart.animateY(2000);
    }

    // 오늘 날짜 구하기
    private String getStringCurrentDate(){
        String result = "2017-01-01";

        try{
            // 오늘 날짜 구하기
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            result = CurDateFormat.format(date);
        }catch (Exception ex){

        }

        return result;
    }

    private String getStringWeekNumberInMonth(String sCurrentDate){
        String result = "2017-01-01";

        try{
            // 오늘 날짜 구하기
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat EngMonth = new SimpleDateFormat("MMM", new Locale("en","US"));
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            result = String.valueOf(cal.get(Calendar.WEEK_OF_MONTH));
            result = cal.get(Calendar.WEEK_OF_MONTH) + " Week of " + EngMonth.format(date);
            // ex : 2 Week of January
        }catch (Exception ex){

        }

        return result;
    }

    private String getStringMonth(String sCurrentDate){
        String result = "2017-01-01";

        try{
            // 오늘 날짜 구하기
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat EngMonth = new SimpleDateFormat("yyyy MMM", new Locale("en","US"));
            Date date = CurDateFormat.parse(sCurrentDate);
            result = EngMonth.format(date);
            // ex : 2017 January
        }catch (Exception ex){

        }

        return result;
    }

    // 기준날짜에 계산하기
    private String getStringCalWeekDate(String sCurrentDate, boolean bPlus7days){
        String result = "2017-01-08";

        try{
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // 기준 날짜에 7일 더하기
            if(bPlus7days) {
                cal.add(Calendar.DATE,7);
            }else{
                cal.add(Calendar.DATE,-7);
            }

            // 계산된 날이 현재 날짜보다 이후이면 현재날짜로 반환하기
            if(cal.getTime().after(new Date(System.currentTimeMillis()))) {
                result = CurDateFormat.format(new Date(System.currentTimeMillis()));
            }else{
                result = CurDateFormat.format(cal.getTime());
            }

        }catch (Exception ex){

        }

        return result;
    }

    // 첫번째 일 마지막 일 구하기
    private String getStringFirstEndMonth(String sCurrentDate){
        String result = "2017-01-08";

        try{
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);



            // 계산된 날이 현재 날짜보다 이후이면 현재날짜로 반환하기
            if(cal.getTime().after(new Date(System.currentTimeMillis()))) {
                result = CurDateFormat.format(new Date(System.currentTimeMillis()));
            }else{
                result = CurDateFormat.format(cal.getTime());
            }

        }catch (Exception ex){

        }

        return result;
    }

}
