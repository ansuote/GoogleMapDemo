package com.lkl.ansuote.demo.googlemapdemo.nearbysearch;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.lkl.ansuote.demo.googlemapdemo.R;
import com.lkl.ansuote.demo.googlemapdemo.base.BaseGoogleMapActivity;
import com.lkl.ansuote.demo.googlemapdemo.base.mode.PlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.mode.places.NearbyPlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.util.HttpUtils;
import com.lkl.ansuote.demo.googlemapdemo.base.util.LocationTransformUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.util.SphericalUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangdongqiang on 04/06/2017.
 */
public class NearbySearchActivity extends BaseGoogleMapActivity {
    private PlacesAdapter mPlacesAdapter;
    private static final int MAX_RESULT_SIZE = 10;
    private float ZOOM_LEVEL = 17;
    private int RANGE = 500;
    private boolean mUseLocationTransform;  //是否使用坐标转换，使用于国内的火星坐标
    private int mLastClickPosition = 0; //上一次点击的item
    private double mLastLatitude;  //上次查询附近点击的纬度
    private double mLastLongitude; //上次查询附近点击的经度

    private List<PlaceBean> mList;
    private static final int DEFAULT_RADIUS = 250;
    private boolean mNearbySearchEnable;  //是否开启附近地点查询 web方式

    @BindView(R.id.listview) ListView mListView;
    private List<PlaceBean> mCurrentPlaces; //暂存地理位置附近的推荐地点，防止重复调用

    @Override
    public void initView() {
        setContentView(R.layout.activity_nearby_search);
        ButterKnife.bind(this);
        initAdapter();
    }

    private void initAdapter() {
        mPlacesAdapter = new PlacesAdapter(this);
        if (null != mListView && null != mPlacesAdapter) {
            mListView.setAdapter(mPlacesAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mLastClickPosition == position) {
                        return;
                    }

                    if (null != mList) {
                        //重制上一次点击的数据
                        if (mList.size() > mLastClickPosition && mLastClickPosition >= 0) {
                            PlaceBean lastBean = mList.get(mLastClickPosition);
                            if (null != lastBean) {
                                lastBean.setSelected(false);
                            }
                        }

                        //设置新的点击项
                        if (mList.size() > position) {
                            PlaceBean bean = mList.get(position);
                            if (null != bean) {
                                bean.setSelected(true);

                                mLastClickPosition = position;
                                moveToPosition(bean.getLatitude(), bean.getLongitude());
                                refreshListView(mList);
                            }
                        }

                    }
                }
            });
        }
    }

    @Override
    public void onMapReadyImp(GoogleMap googleMap) {

        enableMyLocation();
    }

    @Override
    public void onConnectedImp(@Nullable Bundle bundle) {
        mNearbySearchEnable = false;
        getDeviceLocation();
        getCurrentPlaces(MAX_RESULT_SIZE);
    }

    @Override
    public void onConnectionFailedImp(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMyLocationButtonClickImp() {
        mNearbySearchEnable = false;
        if (null == mCurrentPlaces) {
            getCurrentPlaces(MAX_RESULT_SIZE);
        } else {
            resetCurrentPlaces();
            refreshListView(mCurrentPlaces);
            moveToFirstListItem(mCurrentPlaces);
            markFirstLatLng(mCurrentPlaces);
        }
        return false;
    }

    /**
     * 重置 CurrentPlaces ,让其回复默认选择第一项
     */
    private void resetCurrentPlaces() {
        if (null == mCurrentPlaces) {
            return;
        }

        for (int i = 0; i < mCurrentPlaces.size(); i++) {
            PlaceBean bean = mCurrentPlaces.get(i);
            if (null != bean) {
                bean.setSelected(i == 0 ? true : false);
            }
        }
    }

    @Override
    public void onMapClickImp(LatLng latLng) {

    }

    @Override
    public void onMapLongClickImp(LatLng latLng) {

    }

    @Override
    public void onCameraIdleImp() {
        if (!mNearbySearchEnable) {
            return;
        }
        //当前摄像头的位置
        double latitude = mMap.getCameraPosition().target.latitude;
        double longitude = mMap.getCameraPosition().target.longitude;
        // mMap.getCameraPosition().target.longitude)

        //判断是否超过上一次附近地点的范围
        if (isOutOfLastPlaces(mLastLatitude, mLastLongitude, latitude, longitude)) {
            getPlacesByLatLngWeb(latitude, longitude);
        }
    }

    @Override
    public void onPoiClickImp(PointOfInterest pointOfInterest) {

    }

    @Override
    public void onCameraMoveStartedByGestureImp() {
        mNearbySearchEnable = true;
    }

    @Override
    public void onCameraMoveStartedByApiAnimationImp() {
    }

    @Override
    public void onCameraMoveStartedByDeveloperAnimationImp() {
    }

    /**
     * 设置出现 GoogleMap 自带的定位按钮
     */
    private void enableMyLocation() {
        if (checkLocationPermission()) {
            // Access to the location has been granted to the app.
            if (null != mMap) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("lkl", "permission has been granted");

                    //TODO

                } else {
                    Log.i("lkl", "permission has not been granted");
                }
                return;
            }
        }
    }

    /**
     * 获取当前位置附近的地点推荐
     * @param maxSize
     */
    public void getCurrentPlaces(final int maxSize) {
        if (checkLocationPermission()) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);

            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    List<PlaceBean> list = null;
                    if (null != likelyPlaces && likelyPlaces.getCount() > 0) {
                        list = new ArrayList<PlaceBean>();
                        int size = likelyPlaces.getCount() > maxSize ? maxSize : likelyPlaces.getCount();
                        for (int i = 0; i < size; i++) {
                            PlaceLikelihood placeLikelihood = likelyPlaces.get(i);
                            if (null != placeLikelihood && null != placeLikelihood.getPlace()) {
                                PlaceBean bean = getPlaceBean(placeLikelihood.getPlace(), i);
                                if (null != bean) {
                                    list.add(bean);
                                }
                            }
                        }
                        mCurrentPlaces = list;
                        refreshListView(list);
                        moveToFirstListItem(list);
                        markFirstLatLng(list);
                    }

                    likelyPlaces.release();
                }
            });
        }

    }

    private void refreshListView(List<PlaceBean> list) {
        Log.i("lkl", "refreshListView");
        mList = list;
        //刷新适配器
        if (null != mPlacesAdapter) {
            mPlacesAdapter.setData(list);
            mPlacesAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 移动到第一个
     * @param list
     */
    private void moveToFirstListItem(List<PlaceBean> list) {
        if (null != mListView && null != list && list.size() > 0) {
            mListView.smoothScrollToPosition(0);
            mLastClickPosition = 0;
        }
    }

    /**
     * 标记该点
     */
    private void markFirstLatLng(List<PlaceBean> list) {
        if (null != list && list.size() > 0) {
            PlaceBean bean = list.get(0);
            if (null != bean) {
                //标注该点
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(new LatLng(bean.getLatitude(), bean.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_position_small)));
            }
        }
    }

    /**
     * 获取 PlaceBean
     * @param place
     * @param position
     * @return
     */
    private PlaceBean getPlaceBean(Place place, int position) {
        PlaceBean bean = null;
        if (null != place) {
            bean = new PlaceBean();
            bean.setLatitude(place.getLatLng().latitude);
            bean.setSelected(position == 0 ? true : false);
            bean.setLongitude(place.getLatLng().longitude);
            bean.setPlaceName(place.getName()+"");
            bean.setPlacePhone(place.getPhoneNumber()+"");
            bean.setAddress(place.getAddress()+"");
            bean.setId(place.getId());
        }
        return bean;
    }


    /**
     * 获取设备的位置，并且移动到该点
     */
    private void getDeviceLocation() {
        if (checkLocationPermission()) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (null != lastLocation) {
                /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));*/
                moveToPosition(lastLocation.getLatitude(), lastLocation.getLongitude(), ZOOM_LEVEL);

                setLastLatitude(lastLocation.getLatitude(), lastLocation.getLongitude());
            } else {
                //Current location is null. Using defaults
            }
        }
    }

    /**
     * clear marker, Add a marker and move the camera
     * @param latitude
     * @param longitude
     */
    private void moveToPosition(double latitude, double longitude){
        moveToPosition(latitude, longitude, mMap.getCameraPosition().zoom);
    }

    private void moveToPosition(double latitude, double longitude, float zoomLevel){
        LatLng myLoaction = new LatLng(latitude, longitude);
        if (mUseLocationTransform) {
            myLoaction = LocationTransformUtil.transformFromWGSToGCJ(myLoaction);
        }
        if (null != mMap && null != myLoaction) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(myLoaction).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_position_small)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoaction, zoomLevel));
        }
    }


    /**
     * 通过url解析对应的数据，非移动端 API 方式
     */
    private void getPlacesByLatLngWeb(double latitude, double longitude) {
        String url = Util.getGoogleMapPlacesUrl(latitude, longitude);
        if (URLUtil.isNetworkUrl(url)) {
            new NearbyPlacesTask().execute(url);
        }

        setLastLatitude(latitude, longitude);
    }

    class NearbyPlacesTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return Util.returnResult(HttpUtils.doGet(params[0]));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (null != result) {
                JSONArray jsonArray = result.getJSONArray("results");
                if (null != jsonArray) {
                    List<NearbyPlaceBean> list = JSON.parseArray(jsonArray.toString(), NearbyPlaceBean.class);
                    Log.i("lkl", "list = " + list);
                    List<PlaceBean> resultList = formatList(list, MAX_RESULT_SIZE);
                    refreshListView(resultList);
                    moveToFirstListItem(resultList);
                    markFirstLatLng(resultList);
                }
            }

        }
    }

    /**
     * 转化为统一的 bean : PlaceBean
     * @param list
     * @param maxResultSize
     * @return
     */
    private List<PlaceBean> formatList(List<NearbyPlaceBean> list, int maxResultSize) {
        List<PlaceBean> resultList = null;

        if (null != list && list.size() > 0) {
            resultList = new ArrayList<>();
            int size = list.size() > maxResultSize ? maxResultSize : list.size();
            for(int i = 0; i < size; i++) {
                NearbyPlaceBean nearbyPlaceBean = list.get(i);
                PlaceBean placeBean = new PlaceBean();
                if (null != resultList && null != nearbyPlaceBean && null != placeBean) {
                    placeBean.setSelected(i == 0 ? true : false);
                    placeBean.setLatitude(nearbyPlaceBean.getLatitude());
                    placeBean.setLongitude(nearbyPlaceBean.getLongitude());
                    placeBean.setPlaceName(nearbyPlaceBean.getName()+"");
                    //placeBean.setPlacePhone(place.getPhoneNumber()+"");
                    placeBean.setAddress(nearbyPlaceBean.getVicinity()+"");
                    placeBean.setId(nearbyPlaceBean.getPlace_id());
                    resultList.add(placeBean);
                }
            }
        }

        return resultList;
    }

    /**
     * 重置最新的经纬度坐标
     * @param latitude
     * @param longitude
     */
    private void setLastLatitude(double latitude, double longitude) {
        mLastLatitude = latitude;
        mLastLongitude = longitude;
    }

    /**
     * 是否超过了上一次附近地点推荐的范围
     */
    private boolean isOutOfLastPlaces(double fromLat, double fromLng, double toLat, double toLng) {
        double meters = SphericalUtil.computeDistanceBetween(new LatLng(fromLat, fromLng), new LatLng(toLat, toLng));
        Log.i("lkl", "meters = " + meters);
        return meters > DEFAULT_RADIUS;
    }
}
