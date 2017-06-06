package com.lkl.ansuote.demo.googlemapdemo.base.mode.places;

/**
 * Created by huangdongqiang on 05/06/2017.
 */
public class NearbyPlaceBean {
    //geometry
    //icon
    private String id;      //1f7541b5f729cdc8bc8bb546f205848c50315af7
    private String name;    //澎柏白金假日公寓
    //photos
    private String place_id;    //ChIJX9_kRAXsAzQRKmc97njB67c
    //reference
    //scope
    //types
    private String vicinity;    //深圳市宝安区

    private GeometryBean geometry;

    public GeometryBean getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryBean geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }


    public double getLongitude() {
        GeometryBean bean = getGeometry();
        if (null != bean && null != bean.getLocation()) {
            return bean.getLocation().getLng();
        }
        return 0;
    }

    public double getLatitude() {
        GeometryBean bean = getGeometry();
        if (null != bean && null != bean.getLocation()) {
            return bean.getLocation().getLat();
        }
        return 0;
    }
}
