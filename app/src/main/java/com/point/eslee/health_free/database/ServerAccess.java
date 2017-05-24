package com.point.eslee.health_free.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jhchoe on 2017-04-23.
 */

public class ServerAccess {

    // 서버 데이터 받기
    public static String getData(String strUrl) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedInputStream bis = null;
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responseCode;

            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            responseCode = con.getResponseCode();

            if (responseCode == 200) {
                bis = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
                String line = null;

                while ((line = reader.readLine()) != null)
                    sb.append(line);

                bis.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
