package com.lkl.ansuote.demo.googlemapdemo.nearby;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.lkl.ansuote.demo.googlemapdemo.R;
import com.lkl.ansuote.demo.googlemapdemo.base.map.IMap;
import com.lkl.ansuote.demo.googlemapdemo.base.map.MapContract;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.HGoogleMap;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.PlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode.AddressComponent;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode.GeocodeBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.GoogleMapUrlUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.LocationTransformUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.SphericalUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.mvp.BasePresenter;
import com.lkl.ansuote.demo.googlemapdemo.base.util.HttpUtils;

import java.util.List;

import static com.lkl.ansuote.demo.googlemapdemo.nearby.view.NearbyPlacesActivity.REQUEST_CODE_CHECK_GPSSETTINGS;


/**
 * Created by huangdongqiang on 22/05/2017.
 */
public class NearbyPlacesPresenter extends BasePresenter<INearbyPlacesView> {
    private IMap mIMap;
    private boolean mUseLocationTransform;  //是否使用坐标转换，使用于国内的火星坐标
    private List<PlaceBean> mList;
    private int mLastClickPosition = 0; //上一次点击的item
    private float ZOOM_LEVEL = 17;      //摄像头的缩放级别
    private int MAX_RESULT_SIZE = 10;   //最大的推荐结果
    private PlaceBean mSelectedPlaceBean;      //选中的地点
    private boolean mClickSendBtn = false;  //是否已经点击了发送按钮
    private boolean mNearbySearchEnable;  //是否开启附近地点查询 web方式
    private double mLastLatitude;  //上次查询附近点击的纬度
    private double mLastLongitude; //上次查询附近点击的经度
    private List<PlaceBean> mCurrentPlaces; //暂存地理位置附近的推荐地点，防止重复调用
    private static final int DEFAULT_RADIUS = 250;  //附近地点的查找半径（Web接口查找）

    @Override
    public void onStart() {
        if (!isViewAttached()){
            return;
        }

        INearbyPlacesView iNearbyPlacesView = getView();
        if (null != iNearbyPlacesView && iNearbyPlacesView instanceof AppCompatActivity) {
            mIMap = new HGoogleMap((AppCompatActivity) iNearbyPlacesView);
            // onConnected 为事件入口
            regEvent(true);
        }
    }

    /**
     * 判断GPS按钮是否打开
     * @param mIMap
     * @param context
     */
    private void checkGpsSettings(IMap mIMap) {
        mIMap.checkGpsSettings(new MapContract.OnCheckGpsSettingsListener() {
            @Override
            public void onSuccess() {
                getCurrentPlaces();
            }

            @Override
            public int getRequestCode() {
                return REQUEST_CODE_CHECK_GPSSETTINGS;
            }

            @Override
            public void onError() {
                if (isViewAttached()) {
                    getView().showCheckGpsSettingsError();
                }
            }
        });
    }

    /**
     * 授权成功回调
     */
    public void onRequestPermissionsSuccessed() {
        mIMap.enableMyLocation(true);
    }

    /**
     * 注册监听
     * @param b
     */
    private void regEvent(boolean b) {
        if (null != mIMap) {

            mIMap.setOnMapReadyListener(b ? new MapContract.OnMapReadyListener<GoogleMap>() {
                @Override
                public void onMapReady(GoogleMap callback) {
                    mIMap.enableMyLocation(true);
                }
            }: null);

            mIMap.setOnMapClickListener(b ? new MapContract.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng var1) {
                }
            }: null);

            mIMap.setOnConnectionListener(b ? new MapContract.OnConnectionListener() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    checkGpsSettings(mIMap);
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            } : null);

            mIMap.setOnConnectionFailedListener(b ? new MapContract.OnConnectionFailedListener<ConnectionResult>() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult var1) {
                }
            } : null);

            mIMap.setOnMyLocationButtonClickListener(b ? new MapContract.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mNearbySearchEnable = false;
                    if (null == mCurrentPlaces) {
                        getCurrentPlaces();
                    } else {
                        resetCurrentPlaces();
                        refreshListView(mCurrentPlaces);
                        moveToFirstListItem(mCurrentPlaces);
                        markFirstLatLng(mCurrentPlaces);
                    }
                    return false;
                }
            }: null);

            mIMap.setOnCameraIdleListener(b ? new MapContract.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    if (!mNearbySearchEnable) {
                        return;
                    }
                    //当前摄像头的位置
                    double latitude = ((CameraPosition)mIMap.getCameraPosition()).target.latitude;
                    double longitude = ((CameraPosition)mIMap.getCameraPosition()).target.longitude;

                    //判断是否超过上一次附近地点的范围
                    if (isOutOfLastPlaces(mLastLatitude, mLastLongitude, latitude, longitude)) {
                        setLastLatitude(latitude, longitude);
                        mIMap.getPlacesByLatLngWeb(latitude, longitude, MAX_RESULT_SIZE, new MapContract.OnGetPlacesByLatLngWebCallBack() {
                            @Override
                            public void onResult(List<PlaceBean> list) {
                                refreshListView(list);
                                moveToFirstListItem(list);
                                markFirstLatLng(list);
                            }
                        });
                    }
                }
            } : null);
            mIMap.setOnPoiClickListener(new MapContract.OnPoiClickListener() {
                @Override
                public void onPoiClick(Object pointOfInterest) {
                    if (null == mIMap) {
                        return;
                    }
                    mNearbySearchEnable = false;
                    if (null != pointOfInterest && pointOfInterest instanceof PointOfInterest) {
                        mIMap.getPlaceById(((PointOfInterest) pointOfInterest).placeId, MAX_RESULT_SIZE, new MapContract.OnGetPlaceByIdCallback() {
                            @Override
                            public void onResult(List<PlaceBean> list) {
                                moveToFistPosition(list);
                                refreshListView(list);
                            }
                        });
                    }
                }
            });
            mIMap.setOnCameraMoveStartedListener(new MapContract.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int reason) {
                    if (reason == MapContract.OnCameraMoveStartedListener.REASON_GESTURE) {
                        //因为手势而开启移动
                        mNearbySearchEnable = true;

                    } else if (reason == MapContract.OnCameraMoveStartedListener
                            .REASON_API_ANIMATION) {
                        //因为 googleMap 自己调用 API 而移动


                    } else if (reason == MapContract.OnCameraMoveStartedListener
                            .REASON_DEVELOPER_ANIMATION) {
                        //因为 开发者调用 API 而移动

                    }
                }
            });
        }
    }

    /**
     * 清除之前的标记，增加新的标记，移动摄像头（不缩放：保持之前一样的缩放级别）
     * @param latitude
     * @param longitude
     */
    private void moveToPosition(double latitude, double longitude){
        LatLng myLoaction = new LatLng(latitude, longitude);
        if (mUseLocationTransform) {
            myLoaction = LocationTransformUtil.transformFromWGSToGCJ(myLoaction);
        }
        if (null != mIMap && null != myLoaction) {
            mIMap.clearMarker();
            mIMap.addMarker(new MarkerOptions().position(myLoaction).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_position_small)));
            mIMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoaction, ((CameraPosition)mIMap.getCameraPosition()).zoom));
        }
    }

    /**
     * 清除之前的标记，增加新的标记，移动摄像头
     * @param latitude
     * @param longitude
     * @param zoomLevel
     */
    private void moveToPosition(double latitude, double longitude, float zoomLevel){
        LatLng myLoaction = new LatLng(latitude, longitude);
        if (mUseLocationTransform) {
            myLoaction = LocationTransformUtil.transformFromWGSToGCJ(myLoaction);
        }
        if (null != mIMap && null != myLoaction) {
            mIMap.clearMarker();
            mIMap.addMarker(new MarkerOptions().position(myLoaction).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_position_small)));
            mIMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoaction, zoomLevel));
        }
    }

    /**
     * 获取设备的位置，并且移动到该点
     */
    private void getDeviceLocation() {
        Location lastLocation = (Location) mIMap.getLastLocation();
        double latitude = 0;
        double longitude = 0;
        if (null != lastLocation) {
            //定位成功
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();

        } else {
            //TODO 获取地点失败，使用默认定位地点
        
        }

        moveToPosition(latitude, longitude, ZOOM_LEVEL);
        setLastLatitude(latitude, longitude);
    }

    /**
     * 点击查询的某个地点
     * @param place
     */
    public void clickSearchResult(Place place) {
        if (null == place || null == mIMap) {
            return;
        }
        mIMap.getPlaceById(place.getId(), MAX_RESULT_SIZE, new MapContract.OnGetPlaceByIdCallback() {
            @Override
            public void onResult(List<PlaceBean> list) {
                moveToFistPosition(list);
                refreshListView(list);
            }
        });
    }

    /**
     * 获取当前位置附近的地点
     */
    private void getCurrentPlaces() {
        if (null == mIMap) {
            return;
        }

        mIMap.getCurrentPlaces(MAX_RESULT_SIZE, new MapContract.OnGetCurrentPlacesCallBack() {

            @Override
            public void onStart() {
                //尝试初始状态下定位
                getDeviceLocation();
            }

            @Override
            public void onResult(List<PlaceBean> list) {
                if (!isViewAttached()) {
                    return;
                }

                //二次定位。getCurrentPlaces 完成回调时，已经是准确位置
                getDeviceLocation();

                setCurrentPlaces(list);
                refreshListView(list);
                moveToFirstListItem(list);
                markFirstLatLng(list);
            }

            @Override
            public void onPermissionError() {

            }
        });
    }

    /**
     * 保存当前地点的推荐数据
     */
    private void setCurrentPlaces(List<PlaceBean> list) {
        mCurrentPlaces = list;
    }

    /**
     * 移动到第一个点
     * @param list
     */
    private void moveToFistPosition(List<PlaceBean> list) {
        PlaceBean bean = null;
        if (null != list && list.size() > 0) {
            bean = list.get(0);
            if (null != bean) {
                moveToPosition(bean.getLatitude(), bean.getLongitude());
            }

        }

        mSelectedPlaceBean = bean;
    }

    /**
     * 移动到第一个
     * @param list
     */
    private void moveToFirstListItem(List<PlaceBean> list) {
        if (!isViewAttached()) {
            return;
        }
        if (null != list && list.size() > 0) {
            getView().moveToFirstListItem();
            mLastClickPosition = 0;
            mSelectedPlaceBean = list.get(0);
        }
    }

    public void clickItem(int position) {
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
                    mSelectedPlaceBean = bean;
                    moveToPosition(bean.getLatitude(), bean.getLongitude());
                    getView().refreshListView(mList);
                }
            }

        }
    }

    /**
     * 刷新界面
     * @param list
     */
    private void refreshListView(List<PlaceBean> list) {
        if (!isViewAttached()) {
            return;
        }

        //重置数据集
        mList = list;

        //刷新界面
        getView().refreshListView(list);
    }

    /**
     * 点击发送按钮
     */
    public void clickSendBtn() {
        if (!isViewAttached()) {
            return;
        }

        if (mClickSendBtn) {
            return;
        }

        if (null == mSelectedPlaceBean) {
            getView().showChooseLoactionTip();
            return;
        }

        //获取经纬度所在的城市，在完成回调后拍照
        mIMap.getCityByLatLng(mSelectedPlaceBean.getLatitude(), mSelectedPlaceBean.getLongitude(), new MapContract.OnGetCityByLatlngCallback() {
            @Override
            public void onStart() {
                mClickSendBtn = true;
                //显示发送转圈圈
                getView().showLoadingDialog();
            }

            @Override
            public void onResutl(String city) {
                mSelectedPlaceBean.setLocality(city);
                takeSnapshot();
            }

            @Override
            public void onError() {
                if (isViewAttached()) {
                    mClickSendBtn = false;
                    getView().hideLoadingDailog();
                }
            }
        });

    }

    /**
     * 点击确定打开GPS位置信息按钮的回调
     */
    public void onRequestGpsSettings() {
        if (isViewAttached()) {
            getView().showGpsDoingTip();
        }

        getCurrentPlaces();
    }

    class GeocodeTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected com.alibaba.fastjson.JSONObject doInBackground(String... params) {
            return GoogleMapUrlUtil.returnResult(HttpUtils.doGet(params[0]));
        }

        @Override
        protected void onPostExecute(com.alibaba.fastjson.JSONObject result) {
            if (null != result) {
                JSONArray jsonArray = result.getJSONArray("results");
                if (null != jsonArray) {
                    Object firstObj = jsonArray.get(0);
                    if (null != firstObj) {
                        GeocodeBean bean = JSON.parseObject(firstObj.toString(), GeocodeBean.class);
                        String city = getLocality(bean);
                        mSelectedPlaceBean.setLocality(city);
                        takeSnapshot();
                        return;
                    }
                }
            }

            if (isViewAttached()) {
                mClickSendBtn = false;
                getView().hideLoadingDailog();
            }
        }
    }


    /**
     * 从数据集里面获取所在城市
     * @param bean
     * @return
     */
    private String getLocality(GeocodeBean bean) {
        boolean isFound = false;
        if (null != bean) {
            List<AddressComponent> list = bean.getAddress_components();
            if (null != list) {
                for (AddressComponent address : list) {
                    List<String> types = address.getTypes();
                    for (String type : types) {
                        if ("locality".equals(type)) {
                            isFound = true;
                            break;
                        }
                    }
                    if (isFound) {
                        return address.getShort_name();
                    }
                }
            }
        }

        return null;
    }

    /**
     * 快照
     */
    private void takeSnapshot() {
        if (null == mIMap) {
            return;
        }

        if (null != mSelectedPlaceBean) {
            mIMap.takeSnapshot(new MapContract.OnSnapshotReadyListener() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    if (isViewAttached()) {
                        mClickSendBtn = false;
                        getView().hideLoadingDailog();
                        getView().sendLocation(mSelectedPlaceBean, bitmap);
                    }
                }
            });
        } else {
            if (isViewAttached()) {
                mClickSendBtn = false;
                getView().hideLoadingDailog();
                getView().showChooseLoactionTip();
            }

        }
    }

    @Override
    public void detachView() {
        super.detachView();
        if (null != mIMap) {
            mIMap.enableMyLocation(false);
            mIMap.regEvent(false);
            mIMap = null;
        }
        mList = null;
        mSelectedPlaceBean = null;
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
     * 标记该点
     */
    private void markFirstLatLng(List<PlaceBean> list) {
        if (null != list && list.size() > 0) {
            PlaceBean bean = list.get(0);
            if (null != bean) {
                //标注该点
                mIMap.clearMarker();
                mIMap.addMarker(new MarkerOptions().position(new LatLng(bean.getLatitude(), bean.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_position_small)));
            }
        }
    }

    /**
     * 是否超过了上一次附近地点推荐的范围
     */
    private boolean isOutOfLastPlaces(double fromLat, double fromLng, double toLat, double toLng) {
        double meters = SphericalUtil.computeDistanceBetween(new LatLng(fromLat, fromLng), new LatLng(toLat, toLng));
        //Log.i("lkl", "meters = " + meters);
        return meters > DEFAULT_RADIUS;
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
}
