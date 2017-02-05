package com.point.eslee.health_free;

import java.text.DecimalFormat;

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

        }

        return result;
    }
}
