package com.point.eslee.health_free.database;

import android.content.Context;
import android.util.Log;

import com.point.eslee.health_free.VO.RankVO;
import com.point.eslee.health_free.values;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jonehong on 2017-04-16.
 */

public class RankDB {

    Context mCtx;

    public RankDB(Context ctx) {
        this.mCtx = ctx;
    }

    public ArrayList<RankVO> SelectRank(String rank_name) {
        ArrayList<RankVO> result = new ArrayList<RankVO>();
        RankVO rank = null;

        try {
            // 서버에서 랭킹 조회하기
            String url =  "http://dream.miraens.com:58080/healthRank.do?" +
//                String url = "http://192.168.1.160:8087/homeMain01.do?" +
                    "user_id=" + values.UserId + "&rank_type=" + rank_name.toLowerCase();
            String jsonResult = ServerAccess.getData(url);
            JSONObject json = new JSONObject(jsonResult);
            JSONArray jArr = json.getJSONArray("result");

            if(jArr.length() == 0){
                // 랭킹 데이터 없으면 빈 데이터
                for (int i = 0; i < 3; i++) {
                    rank = new RankVO(i, -1 , i+1, "image", "nothing user", "nothing value");
                    result.add(rank);
                }
            }else {
                for(int i=0; i<jArr.length(); i++){
                    JSONObject jsonObject = jArr.getJSONObject(i);
                    try{
                        rank = new RankVO(i, jsonObject.getInt("user_id"), i+1, jsonObject.getString("img_url"), jsonObject.getString("user_name"), jsonObject.getString("rank_value"));
                    }catch (Exception ex){
                        rank = new RankVO(i, -1, i+1, "image", "nothing user", "nothing value");
                    }
                    result.add(rank);
                }
            }

        } catch (Exception ex) {
            Log.e("RankDB : ", ex.getMessage());
            // 랭킹 데이터 없으면 빈 데이터
            for (int i = 0; i < 3; i++) {
                rank = new RankVO(i, -1, i+1, "image", "nothing user", "nothing value");
                result.add(rank);
            }
        }

        return result;
    }

    public void UpdateRankInfo(int step, Double distance, int calorie, int t_point, int r_time){
        try {
            // 서버에 운동기록 및 포인트 정보 업데이트
            String url =  "http://dream.miraens.com:58080/updateUserInfo.do?" +
                    "user_id=" + values.UserId + "&steps=" + step + "&distance=" + distance + "&calorie=" + calorie + "&r_time=" + r_time + "&t_point=" + t_point;
            String jsonResult = ServerAccess.getData(url);
        } catch (Exception ex) {
            Log.e("RankDB : ", ex.getMessage());
        }
    }


}
