package com.point.eslee.health_free.point;

/**
 * Created by eslee on 2017-02-12.
 */

public class ListViewPointItem {
    private int _id;
    private String titleStr;
    private String datetimeStr;
    private int point_value;
    private String point_type;

    public ListViewPointItem(int id, String pointType, String title, int point, String datetime){
        _id = id;
        titleStr = title;
        datetimeStr = datetime;
        point_value = point;
        point_type = pointType;
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

    public void setPointType(String pointType) {
        point_type = pointType;
    }

    public String getPointType() {
        return this.point_type;
    }

}
