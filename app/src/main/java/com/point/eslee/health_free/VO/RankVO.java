package com.point.eslee.health_free.VO;

/**
 * Created by Administrator on 2017-04-14.
 */

public class RankVO {
    public int _ID;
    public int Num;
    public String imgUrl;
    public String Title;
    public String Value;

    public RankVO(int _ID, int num, String imgurl, String title, String value) {
        this._ID = _ID;
        Num = num;
        imgUrl = imgurl;
        Title = title;
        Value = value;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public int get_ID() {
        return _ID;
    }

    public void set_ID(int _ID) {
        this._ID = _ID;
    }

    public int getNum() {
        return Num;
    }

    public void setNum(int num) {
        Num = num;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}

