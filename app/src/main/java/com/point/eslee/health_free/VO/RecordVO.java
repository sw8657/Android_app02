package com.point.eslee.health_free.VO;

/**
 * Created by jonehong on 2017-04-16.
 */

public class RecordVO {

    public int _ID;
    public String CreateDate;
    public int Steps;
    public double Distance;
    public double Calorie;
    public int TotalPoint;
    public int RunningTime;

    public RecordVO(int _ID, String createDate, int steps, double distance, double calorie, int totalPoint, int runningTime) {
        this._ID = _ID;
        CreateDate = createDate;
        Steps = steps;
        Distance = distance;
        Calorie = calorie;
        TotalPoint = totalPoint;
        RunningTime = runningTime;
    }

    public RecordVO(){

    }

    public int get_ID() {
        return _ID;
    }

    public void set_ID(int _ID) {
        this._ID = _ID;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public int getSteps() {
        return Steps;
    }

    public void setSteps(int steps) {
        Steps = steps;
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }

    public double getCalorie() {
        return Calorie;
    }

    public void setCalorie(double calorie) {
        Calorie = calorie;
    }

    public int getTotalPoint() {
        return TotalPoint;
    }

    public void setTotalPoint(int totalPoint) {
        TotalPoint = totalPoint;
    }

    public int getRunningTime() {
        return RunningTime;
    }

    public void setRunningTime(int runningTime) {
        RunningTime = runningTime;
    }

}
