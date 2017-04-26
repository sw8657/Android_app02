package com.point.eslee.health_free.VO;

/**
 * Created by eslee on 2017-04-04.
 */

public class MyPointVO {

    public int _ID;
    public String UseType;
    public String UseTitle;
    public int UsePoint;
    public String CreateDate;
    public int StoreID;

    public MyPointVO(int _ID, String useType, String useTitle, int usePoint, String createDate) {
        this._ID = _ID;
        this.UseType = useType;
        this.UseTitle = useTitle;
        this.UsePoint = usePoint;
        this.CreateDate = createDate;
    }

    public MyPointVO() {

    }

    public int getStoreID() {
        return StoreID;
    }

    public void setStoreID(int storeID) {
        StoreID = storeID;
    }

    public int get_ID() {
        return _ID;
    }

    public void set_ID(int _ID) {
        this._ID = _ID;
    }

    public String getUseType() {
        return UseType;
    }

    public void setUseType(String useType) {
        UseType = useType;
    }

    public String getUseTitle() {
        return UseTitle;
    }

    public void setUseTitle(String useTitle) {
        UseTitle = useTitle;
    }

    public int getUsePoint() {
        return UsePoint;
    }

    public void setUsePoint(int usePoint) {
        UsePoint = usePoint;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }


}
