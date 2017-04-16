package com.point.eslee.health_free;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by eslee on 2017-02-05.
 */

public class Common {

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
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    public static String get_commaString(double num){
        String result = String.valueOf(num);

        try {
            Long L = Math.round(num);
            int intValue = L.intValue();
            DecimalFormat df = new DecimalFormat("#,##0");
            result = df.format(intValue).toString();
        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    /**
     * 숫자 -> 콤마 문자열 변환
     *
     * @param num
     * @return
     */
    public static String get_commaString(int num) {
        String result = String.valueOf(num);

        try {
            int intValue = num;
            DecimalFormat df = new DecimalFormat("#,##0");
            result = df.format(intValue).toString();
        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 포인트가 양수 음수에 따라서
    public static String get_pointString(int point){
        String result = get_commaString(point);

        try{
            if(point > 0){
                result = "+" + result;
            }
        }catch (Exception ex){
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 오늘 날짜 구하기
    public static String getStringCurrentDate() {
        String result = "2017-01-01";

        try {
            // 오늘 날짜 구하기
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            result = CurDateFormat.format(date);
        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 날짜 기준 이번주 기간 문자열 구하기
    public static String getStringWeekNumberInMonth(String sCurrentDate) {
        String result = "2017-01-01";

        try {
            // 오늘 날짜 구하기
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat EngMonth = new SimpleDateFormat("MMM", new Locale("en", "US"));
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            result = String.valueOf(cal.get(Calendar.WEEK_OF_MONTH));
            result = cal.get(Calendar.WEEK_OF_MONTH) + " Week of " + EngMonth.format(date);
            // ex : 2 Week of January
        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 날짜 기준 이번달 문자열 구하기
    public static String getStringMonth(String sCurrentDate) {
        String result = "2017-01-01";

        try {
            // 오늘 날짜 구하기
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat EngMonth = new SimpleDateFormat("yyyy MMMM", new Locale("en", "US"));
            Date date = CurDateFormat.parse(sCurrentDate);
            result = EngMonth.format(date);
            // ex : 2017 January
        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 주간 날짜 계산하기 (7일 더하기 빼기)
    public static String getStringCalWeekDate(String sCurrentDate, boolean bPlus7days) {
        String result = "2017-01-08";

        try {
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // 기준 날짜에 7일 더하기
            if (bPlus7days) {
                cal.add(Calendar.DATE, 7);
            } else {
                cal.add(Calendar.DATE, -7);
            }

            // 계산된 날이 현재 날짜보다 이후이면 현재날짜로 반환하기
            if (cal.getTime().after(new Date(System.currentTimeMillis()))) {
                result = CurDateFormat.format(new Date(System.currentTimeMillis()));
            } else {
                result = CurDateFormat.format(cal.getTime());
            }

        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 월간 날짜 계산하기 (한달 더하기 빼기)
    public static String getStringCalMonthDate(String sCurrentDate, boolean bPlusMonth) {
        String result = "2017-01-08";

        try {
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // 기준 날짜에 1개월 더하기
            if (bPlusMonth) {
                cal.add(Calendar.MONTH, 1);
            } else {
                cal.add(Calendar.MONTH, -1);
            }

            // 계산된 날이 현재 날짜보다 이후이면 현재날짜로 반환하기
            if (cal.getTime().after(new Date(System.currentTimeMillis()))) {
                result = CurDateFormat.format(new Date(System.currentTimeMillis()));
            } else {
                result = CurDateFormat.format(cal.getTime());
            }

        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 월간 첫번째 일 마지막 일 구하기
    public static String[] getStringFirstEndMonth(String sCurrentDate) {
        String[] result = new String[2];

        try {
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat yyyyMMFormat = new SimpleDateFormat("yyyy-MM");
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            result[0] = yyyyMMFormat.format(date) + "-01"; // 첫번째 날
            result[1] = yyyyMMFormat.format(date) + "-" + cal.getMaximum(Calendar.DAY_OF_MONTH); // 마지막날
        } catch (Exception ex) {
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 해당 월의 일수 계산
    public static int getDaysOfMonth(String sCurrentDate){
        int result = 30;

        try{
            // 기준 날짜 구하기
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = CurDateFormat.parse(sCurrentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            result = cal.getMaximum(Calendar.DAY_OF_MONTH);
        }catch (Exception ex){
            Log.e("Error : ", ex.getMessage());
        }

        return result;
    }

    // 현재 운동 시간 조회하기
    public static String getRunningTimeString(){
        String returnTime = "00:00:00";

        Calendar cal = Calendar.getInstance();
        //경과시간
        long runningTime = 0 ;
        if(values.StartTime != null){
            runningTime = Calendar.getInstance().getTimeInMillis() - values.StartTime.getTimeInMillis();

            Date formatTime = new Date(runningTime);
            cal.setTime(formatTime);

            // 시간:분 타입으로 포맷
            returnTime = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
        }

        return returnTime;
    }

    // 현재 운동 시간 조회하기
    public static long getRunningTime(){
        long runningTime = 0 ;
        Calendar cal = Calendar.getInstance();
        //경과시간

        if(values.StartTime != null){
            runningTime = Calendar.getInstance().getTimeInMillis() - values.StartTime.getTimeInMillis();
        }

        return runningTime;
    }

    // 현재 운동 시간 조회하기
    public static int getRunningTimeSecond(){
        int result = 0;
        long runningTime = 0 ;
        Calendar cal = Calendar.getInstance();
        //경과시간

        if(values.StartTime != null){
            runningTime = Calendar.getInstance().getTimeInMillis() - values.StartTime.getTimeInMillis();
            result = (int)(runningTime) / 1000;
        }

        return result;
    }

    public static String convertTimetoString(long time){
        String result = "00:00:00";

        try{
            Calendar cal = Calendar.getInstance();
            Date formatTime = new Date(time);
            cal.setTime(formatTime);

            // 시간:분 타입으로 포맷
            result = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
        }catch (Exception ex){
            Log.e("Common : ", ex.getMessage());
        }

        return result;
    }

    public static double getRunningHour(long time){
        double result = 0;

        try{
            int time2 = (int)(time) / 1000;
            result = time2 / 3600;

        }catch (Exception ex){
            Log.e("Common : ", ex.getMessage());
        }

        return result;
    }

}
