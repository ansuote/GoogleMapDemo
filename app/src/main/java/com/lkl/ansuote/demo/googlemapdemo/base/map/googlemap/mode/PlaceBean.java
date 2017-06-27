package com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode;

/**
 * Created by huangdongqiang on 23/05/2017.
 */
public class PlaceBean {
    private String mId;
    private double mLatitude;
    private double mLongitude;
    private String mPlaceName;
    private String mPlacePhone;
    //城市
    private String mLocality;
    //地名
    private String mFeatureName;
    private String mAddress;
    private boolean mSelected;  //是否被选中

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public void setPlaceName(String placeName) {
        mPlaceName = placeName;
    }

    public String getPlacePhone() {
        return mPlacePhone;
    }

    public void setPlacePhone(String placePhone) {
        mPlacePhone = placePhone;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public String getLocality() {
        return mLocality;
    }

    public void setLocality(String locality) {
        mLocality = locality;
    }

    public String getFeatureName() {
        return mFeatureName;
    }

    public void setFeatureName(String featureName) {
        mFeatureName = featureName;
    }
}
