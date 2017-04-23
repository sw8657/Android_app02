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

    public static boolean bSuccess = false;
    public static String user_id = "";
    public static String user_name = "";

    public static String getData(String strUrl) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedInputStream bis = null;
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responseCode;

            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

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

    public static boolean getLoginInfo(String jsonString){
        boolean result = false;

        try{
            // 안드로이드 JSON 파싱 로직
            JSONObject json = new JSONObject(jsonString);
            JSONArray jArr = json.getJSONArray("result");

            bSuccess = jArr.getJSONObject(0).getString("successYn").equals("SUCCESS");
            user_id = jArr.getJSONObject(0).getString("user_id");
            user_name = jArr.getJSONObject(0).getString("userNm");

            result =true;
//            for(int i=0;i<jArr.length();i++)
//            {
//                JSONObject jObject = jArr.getJSONObject(i);
//
//            }
        }catch (Exception ex){

        }

        return result;
    }
}
