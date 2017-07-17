package com.lkl.ansuote.demo.googlemapdemo.base.map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.GoogleMap;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.PlaceBean;

import java.util.List;

/**
 * Created by huangdongqiang on 23/05/2017.
 */
public interface IMap<T, M, V, C, L> {
    /**
     * 初始化地图
     */
    void init();

    /**
     * 注册地图监听
     * @param b
     */
    void regEvent(boolean b);

    /**
     * 拍快照
     */
    void takeSnapshot(MapContract.OnSnapshotReadyListener listener);

    /**
     * 清理标注
     */
    void clearMarker();

    /**
     * 增加标注
     * @param latitude
     * @param longitude
     */
    void addMarker(double latitude, double longitude);

    void addMarker(M markerOptions);

    /**
     * 地图是否加载完成
     * @return  true：加载完成； false：还没加载完成
     */
     boolean isMapAttached();

    /**
     * 移动相机
     */
    void moveCamera(double latitude, double longitude);

    void moveCamera(C cameraUpdate);

    /**
     * 定义移动相机的动画
     */
    void animateCamera(C cameraUpdate, int i);

    /**
     * 设置是否等待地图加载完成
     * @param waitForMapLoaded
     */
    void setWaitForMapLoaded(boolean waitForMapLoaded);


    /**
     * 设置地图加载完毕回调
     * @param listener
     */
    void setOnMapReadyListener(MapContract.OnMapReadyListener<GoogleMap> listener);

    /**
     * 设置 ApiClient 回调
     * @param listener
     */
    void setOnConnectionListener(MapContract.OnConnectionListener listener);

    /**
     * 设置连接失败回调
     * @param listener
     */
    void setOnConnectionFailedListener(MapContract.OnConnectionFailedListener<ConnectionResult> listener);

    /**
     * 设置点击定位按钮回调
     * @param onMyLocationButtonClickListener
     */
    void setOnMyLocationButtonClickListener(MapContract.OnMyLocationButtonClickListener onMyLocationButtonClickListener);

    /**
     * 设置点击地图上的点回调
     * @param onMapClickListener
     */
    void setOnMapClickListener(MapContract.OnMapClickListener onMapClickListener);

    /**
     * 设置长按地图上的点回调
     * @param onMapLongClickListener
     */
    void setOnMapLongClickListener(MapContract.OnMapLongClickListener onMapLongClickListener);

    /**
     * 设置相机停止时候的回调
     * @param onCameraIdleListener
     */
    void setOnCameraIdleListener(final MapContract.OnCameraIdleListener onCameraIdleListener);

    /**
     * 设置点击地点的时候的回调
     * @param onPoiClickListener
     */
    void setOnPoiClickListener(MapContract.OnPoiClickListener onPoiClickListener);

    /**
     * 使能我的位置
     * @param enable
     */
    void enableMyLocation(boolean enable);

    T getLastLocation();

    /**
     * 获取当前地点附近的地点数据
     * @return
     */
    void getCurrentPlaces(int maxSize, MapContract.OnGetCurrentPlacesCallBack callBack);

    /**
     * 通过经纬度查询到当前地点附近信息
     * 后台线程调用
     * @return
     */
    List<PlaceBean> getPlacesByLatLngInBackground(double latitude, double longitude, int maxResults);

    /**
     * 通过经纬度查询到当前地点附近信息
     * @param latitude
     * @param longitude
     * @param maxResults
     * @param callBack
     */
    void getPlacesByLatLng(double latitude, double longitude, int maxResults, MapContract.OnGetPlacesByLatLngCallBack callBack);


    /**
     * 通过 Place ID 获取地点附近的推荐
     * @param id
     */
    void getPlaceById(String id, int maxResult, MapContract.OnGetPlaceByIdCallback callback);


    /**
     * 通过url解析对应的数据，非移动端 API 方式
     * @param latitude
     * @param longitude
     * @param maxResult
     * @param callback
     */
    void getPlacesByLatLngWeb(double latitude, double longitude, int maxResult, MapContract.OnGetPlacesByLatLngWebCallBack callback);

    /**
     * 根据经纬度获取所在城市
     * @param latitude
     * @param longitude
     * @param callback
     */
    void getCityByLatLng(double latitude, double longitude, MapContract.OnGetCityByLatlngCallback callback);

    /**
     * 根据经纬度获取地理位置信息（包括所在城市）
     * @param latitude
     * @param longitude
     * @param callback
     */
    void getGeocodeByLatLng(double latitude, double longitude, MapContract.OnGetGeocodeByLatLngCallback callback);

    /**
     * 获取相机的位置
     * @return
     */
    V getCameraPosition();

    /**
     * 连接client
     */
    void connect();

    /**
     * 断开 client 连接
     */
    void disconnect();

    /**
     * 请求位置变化的监听
     */
    void regLocationUpdates(MapContract.OnLocationListener<L> listener);

    /**
     * 移除位置变化的监听
     */
    void removeLocationUpdates();

    /**
     * 摄像头开始的监听
     * @param listener
     */
    void setOnCameraMoveStartedListener(MapContract.OnCameraMoveStartedListener listener);

    /**
     * 检测是否已经打开了定位按钮【位置信息】
     */
    void checkGpsSettings(MapContract.OnCheckGpsSettingsListener listener);

    /**
     * 手动销毁
     */
    void onDestory();

}
