package com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by huangdongqiang on 31/05/2017.
 */
public class GoogleMapUrlUtil {
    //private static final String GOOGLE_MAP_URL = "http://maps.google.cn/maps/api/geocode/json?language=zh-CN&sensor=true&latlng=%1$s,%2$s";
    private static final String GOOGLE_MAP_URL = "https://maps.google.com/maps/api/geocode/json?language=%1$s&sensor=true&latlng=%2$s,%3$s";
    private static final String GOOGLE_MAP_PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?language=%1$s&location=%2$s,%3$s&radius=%4$s&type=%5$s&key=%6$s";
    private static final String KEY = "AIzaSyDNdPWjhPO8myhEh6z8u4Cndmsnm5WWVbQ";
    private static final String DEFAULT_LANGUAGE = "zh-CN";
    private static final int DEFAULT_RADIUS = 250;
    private static final String DEFAULT_TYPE = "point_of_interest";

    /**
     * 拼接url(默认设置语言为中文)
     *
     * @param latitude
     * @param longitude
     */
    public static String getGoogleMapUrl(double latitude, double longitude) {
        return String.format(GOOGLE_MAP_URL, DEFAULT_LANGUAGE, Double.valueOf(latitude), Double.valueOf(longitude));
    }

    /**
     * 拼接url
     *
     * @param latitude
     * @param longitude
     * @param language
     * @return
     */
    public static String getGoogleMapUrl(double latitude, double longitude, String language) {
        return String.format(GOOGLE_MAP_URL, language, Double.valueOf(latitude), Double.valueOf(longitude));
    }


    public static JSONObject returnResult(String result) {
        try {
            if (!TextUtils.isEmpty(result)) {
                return JSON.parseObject(result);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 拼接获取地点推荐的url
     * @return
     */
    public static String getGoogleMapPlacesUrl(String language, double latitude, double longitude, int radius, String type, String KEY) {
        return String.format(GOOGLE_MAP_PLACES_URL, language, latitude, longitude, radius, type, KEY);
    }

    public static String getGoogleMapPlacesUrl(double latitude, double longitude) {
        return getGoogleMapPlacesUrl(DEFAULT_LANGUAGE, latitude, longitude, DEFAULT_RADIUS, DEFAULT_TYPE, KEY);
    }
}
