package com.point.eslee.health_free;

/**
 * Created by eslee on 2017-02-12.
 */

public class ListViewPointItem {
    private int _id;
    private String titleStr;
    private String datetimeStr;
    private int point_value;
    private String pointStr;

    public ListViewPointItem(int id, String title, String datetime, int point, String pointString){
        _id = id;
        titleStr = title;
        datetimeStr = datetime;
        point_value = point;
        pointStr = pointString;
    }

    public void setId(int id) {
        _id = id;
    }

    public int getId() {
        return this._id;
    }

    public void setTitle(String title) {
        titleStr = title;
    }

    public String getTitle() {
        return this.titleStr;
    }

    public void setDateTime(String datetime) {
        datetimeStr = datetime;
    }

    public String getDateTime() {
        return this.datetimeStr;
    }

    public void setPoint(int point) {
        point_value = point;
    }

    public int getPoint() {
        return this.point_value;
    }

    public void setPointStr(String pointString) {
        pointStr = pointString;
    }

    public String getPointStr() {
        return this.pointStr;
    }

}
