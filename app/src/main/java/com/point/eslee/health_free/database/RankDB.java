package com.point.eslee.health_free.database;

import android.content.Context;
import android.util.Log;

import com.point.eslee.health_free.VO.RankVO;

import java.util.ArrayList;

/**
 * Created by jonehong on 2017-04-16.
 */

public class RankDB {

    Context mCtx;

    public RankDB(Context ctx) {
        this.mCtx = ctx;
    }

    public ArrayList<RankVO> SelectRankPoint() {
        ArrayList<RankVO> result = new ArrayList<RankVO>();
        RankVO rank = null;

        try {
            // TODO: 서버에서 랭킹 조회하기

            // 랭킹 데이터 없으면 빈 데이터
            for (int i = 1; i <= 3; i++) {
                rank = new RankVO(i, i, "nothing user", "nothing value");
                result.add(rank);
            }

        } catch (Exception ex) {
            Log.e("RankDB : ", ex.getMessage());
        }

        return result;
    }

    // 내 포인트 랭킹 조회하기
    public RankVO SelectMyRankPoint(){
        RankVO result = null;
        try {
            result = new RankVO(1,1,"nothing user","nothing value");

            // TODO: 서버에서 내 랭킹 조회하기

        }catch (Exception ex){
            Log.e("RankDB : ", ex.getMessage());
        }
        return result;
    }

}
