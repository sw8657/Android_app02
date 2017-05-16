package com.point.eslee.health_free.database;

import android.content.Context;
import android.util.Log;

import com.point.eslee.health_free.VO.RankVO;
import com.point.eslee.health_free.values;

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
            // TODO: 서버에서 랭킹 조회하기

            // 랭킹 데이터 없으면 빈 데이터
            for (int i = 1; i <= 3; i++) {
                rank = new RankVO(i, i, "image", "nothing user", "nothing value");
                result.add(rank);
            }

        } catch (Exception ex) {
            Log.e("RankDB : ", ex.getMessage());
        }

        return result;
    }

    // 내 포인트 랭킹 조회하기
    public RankVO SelectMyRank(String rank_name) {
        RankVO result = null;
        Boolean bSuccess = false;
        Integer _id = 1, num = 1;
        String value = "nothing value";
        try {
            // TODO: 서버에서 내 랭킹 조회하기
            // _ID, 순위, 프로필이미지, 이름, 값
            if(bSuccess){

            }

            result = new RankVO(_id, num, values.UserImageUrl, values.UserName, value);

        } catch (Exception ex) {
            Log.e("RankDB : ", ex.getMessage());
        }
        return result;
    }


}
