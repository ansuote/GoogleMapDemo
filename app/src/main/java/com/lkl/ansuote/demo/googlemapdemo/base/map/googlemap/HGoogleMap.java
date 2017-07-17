package com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.URLUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.lkl.ansuote.demo.googlemapdemo.base.map.IMap;
import com.lkl.ansuote.demo.googlemapdemo.base.map.MapContract;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.PlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode.AddressComponent;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode.GeocodeBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.places.NearbyPlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.GoogleMapUrlUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.PermissionUtils;
import com.lkl.ansuote.demo.googlemapdemo.base.util.HttpUtils;

import java.util.ArrayList;
import java.util.List;

import static com.lkl.ansuote.demo.googlemapdemo.R.id.map;

/**
 * Created by huangdongqiang on 23/05/2017.
 */
public class HGoogleMap implements IMap<Location, MarkerOptions, CameraPosition, CameraUpdate, Location> {
    private FragmentActivity mContext;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private MapContract.OnMapReadyListener<GoogleMap> mOnMapReadyListener;
    private MapContract.OnConnectionListener mConnectionListener;
    private MapContract.OnConnectionFailedListener<ConnectionResult> mOnConnectionFailedListener;
    private MapContract.OnMyLocationButtonClickListener mOnMyLocationButtonClickListener;
    private MapContract.OnMapClickListener mOnMapClickListener;
    private MapContract.OnMapLongClickListener mOnMapLongClickListener;
    private MapContract.OnCheckGpsSettingsListener mOnCheckGpsSettingsListener;
    private MapContract.OnCameraIdleListener mOnCameraIdleListener;
    private MapContract.OnLocationListener<Location> mOnLocationListener;
    private MapContract.OnPoiClickListener<PointOfInterest> mOnPoiClickListener;
    private MapContract.OnCameraMoveStartedListener mOnCameraMoveStartedListener;
    private MapContract.OnGetPlacesByLatLngWebCallBack mOnGetPlacesByLatLngWebCallBack;
    private MapContract.OnGetCityByLatlngCallback mOnGetCityByLatlngCallback;
    private MapContract.OnGetGeocodeByLatLngCallback mOnGetGeocodeByLatLngCallback;


    private boolean mWaitForMapLoaded;  //等待地图加载完再拍快照
    private Geocoder mGeocoder;
    private Handler mHandler;

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int REQUEST_CODE_AUTOCOMPLETE = 2;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 1500; /* 1.5 sec */

    private LocationRequest mLocationRequest;
    private int maxResultSize = 20;

    /**
     * 启动定位和地图功能
     * @param appCompatActivity
     */
    public HGoogleMap(AppCompatActivity appCompatActivity) {
        mContext = appCompatActivity;
        init();
    }

    /**
     * 只启动定位功能
     */
    public HGoogleMap(FragmentActivity context) {
        mContext = context;
        createGoogleApiClient();
    }

    @Override
    public void init() {
        initGoogleMap();
        createGoogleApiClient();
        //mGeoCoder = GeoCoder.newInstance();
        mGeocoder = new Geocoder(mContext);
        mHandler = new Handler();
    }

    @Override
    public void regEvent(boolean b) {
        mMap.setOnMyLocationButtonClickListener(b ? new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (null != mOnMyLocationButtonClickListener) {
                    mOnMyLocationButtonClickListener.onMyLocationButtonClick();
                }
                return false;
            }
        } : null);
        mMap.setOnMapClickListener(b ? new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (null != mOnMapClickListener) {
                    mOnMapClickListener.onMapClick(latLng);
                }
            }
        } : null);
        mMap.setOnMapLongClickListener(b ? new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (null != mOnMapLongClickListener) {
                    mOnMapLongClickListener.onMapLongClick(latLng);
                }
            }
        } : null);
        mMap.setOnCameraIdleListener(b ? new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (null != mOnCameraIdleListener) {
                    mOnCameraIdleListener.onCameraIdle();
                }
            }
        } : null);
        //点击地图上的景点，可以返回包含经度/维度坐标、地点 ID 以及景点名称
        mMap.setOnPoiClickListener(b ? new GoogleMap.OnPoiClickListener(){
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                if (null != mOnPoiClickListener) {
                    mOnPoiClickListener.onPoiClick(pointOfInterest);
                }
            }
        } : null);
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (null != mOnCameraMoveStartedListener) {
                    mOnCameraMoveStartedListener.onCameraMoveStarted(i);
                }
            }
        });
    }

    @Override
    public void takeSnapshot(final MapContract.OnSnapshotReadyListener listener) {
        if (!isMapAttached()) {
            return;
        }

        if (mWaitForMapLoaded) {
            //等待照片加载完成在拍照
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    if (null != mMap) {
                        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap bitmap) {
                                if (null != listener) {
                                    listener.onSnapshotReady(bitmap);
                                }
                            }
                        });
                    }
                }
            });
        } else {
            mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    if (null != listener) {
                        listener.onSnapshotReady(bitmap);
                    }
                }
            });
        }
    }

    @Override
    public void clearMarker() {
        if (!isMapAttached()) {
            return;
        }

        mMap.clear();
    }

    @Override
    public void addMarker(double latitude, double longitude) {
        if (!isMapAttached()) {
            return;
        }

        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location));
    }

    @Override
    public void addMarker(MarkerOptions markerOptions) {
        if (!isMapAttached()) {
            return;
        }

        mMap.addMarker(markerOptions);
    }

    @Override
    public void moveCamera(double latitude, double longitude) {
        if (!isMapAttached()) {
            return;
        }

        LatLng location = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoaction, 12));

    }

    @Override
    public void moveCamera(CameraUpdate cameraUpdate) {
        if (!isMapAttached() || null == cameraUpdate) {
            return;
        }

        mMap.moveCamera(cameraUpdate);
    }

    @Override
    public void animateCamera(CameraUpdate cameraUpdate, int i) {
        if (!isMapAttached()) {
            return;
        }
        if (null != cameraUpdate) {
            mMap.animateCamera(cameraUpdate, i, null);
        }
    }

    @Override
    public boolean isMapAttached() {
        return null != mMap;
    }

    @Override
    public void onDestory() {
        regEvent(false);

        mContext = null;
        mMap = null;
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        mOnMapReadyListener = null;
        mConnectionListener = null;
        mOnConnectionFailedListener = null;
        mOnMyLocationButtonClickListener = null;
        mOnMapClickListener = null;
        mOnMapLongClickListener = null;
        mOnCameraIdleListener = null;
        mGeocoder = null;
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private void initGoogleMap() {
        if (null != mContext) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) mContext.getSupportFragmentManager()
                    .findFragmentById(map);

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    /*if (null != mMap) {
                        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }*/
                    mMap.setBuildingsEnabled(true);
                    mMap.setIndoorEnabled(true);
                    regEvent(true);

                    if (checkLocationPermission()) {
                        //地图加载完毕后，回调给外面做处理（比如增加标注，定位）
                        if (null != mOnMapReadyListener) {
                            mOnMapReadyListener.onMapReady(googleMap);
                        }
                    }
                }
            });
        }
    }

    /**
     * 初始化 google client 用于获取地点信息
     */
    private void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(mContext)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            if (null != mConnectionListener) {
                                mConnectionListener.onConnected(bundle);
                            }
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            if (null != mConnectionListener) {
                                mConnectionListener.onConnectionSuspended(i);
                            }
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            if (null != mOnConnectionFailedListener) {
                                mOnConnectionFailedListener.onConnectionFailed(connectionResult);
                            }
                        }
                    })
                    .enableAutoManage(mContext, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            if (null != mOnConnectionFailedListener) {
                                mOnConnectionFailedListener.onConnectionFailed(connectionResult);
                            }
                        }
                    })
                    .build();
            //mGoogleApiClient.connect();
        }
    }

    /**
     * 检测是否已经打开了定位按钮【位置信息】
     * 谷歌地图专用
     * @param listener
     */
    @Override
    public void checkGpsSettings(MapContract.OnCheckGpsSettingsListener listener) {
        mOnCheckGpsSettingsListener = listener;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationSettingsRequest.Builder builder =  new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                if (null == mOnCheckGpsSettingsListener) {
                    return;
                }
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        mOnCheckGpsSettingsListener.onSuccess();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    mContext, mOnCheckGpsSettingsListener.getRequestCode());
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            mOnCheckGpsSettingsListener.onError();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        mOnCheckGpsSettingsListener.onError();
                        break;
                }
            }
        });
    }

    /**
     * 注册位置改变接口，在 onConnected 之后才能调用
     */
    @Override
    public void regLocationUpdates(final MapContract.OnLocationListener<Location> listener) {
        mOnLocationListener = listener;

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, mLocationListener);
    }



    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (null != mOnLocationListener) {
                mOnLocationListener.onLocationChanged(location);
            }
        }
    };


    @Override
    public void removeLocationUpdates() {
        if (null != mLocationRequest) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
    }


    @Override
    public void setWaitForMapLoaded(boolean waitForMapLoaded) {
        mWaitForMapLoaded = waitForMapLoaded;
    }


    @Override
    public void setOnMapReadyListener(MapContract.OnMapReadyListener<GoogleMap> listener) {
        mOnMapReadyListener = listener;
    }

    @Override
    public void setOnConnectionListener(MapContract.OnConnectionListener listener) {
        mConnectionListener = listener;
    }

    @Override
    public void setOnConnectionFailedListener(MapContract.OnConnectionFailedListener<ConnectionResult> listener) {
        mOnConnectionFailedListener = listener;
    }

    @Override
    public void setOnMyLocationButtonClickListener(MapContract.OnMyLocationButtonClickListener onMyLocationButtonClickListener) {
        mOnMyLocationButtonClickListener = onMyLocationButtonClickListener;
    }

    @Override
    public void setOnMapClickListener(MapContract.OnMapClickListener onMapClickListener) {
        mOnMapClickListener = onMapClickListener;
    }

    @Override
    public void setOnMapLongClickListener(MapContract.OnMapLongClickListener onMapLongClickListener) {
        mOnMapLongClickListener = onMapLongClickListener;
    }

    @Override
    public void setOnCameraIdleListener(final MapContract.OnCameraIdleListener onCameraIdleListener) {
        mOnCameraIdleListener = onCameraIdleListener;
    }

    @Override
    public void setOnPoiClickListener(MapContract.OnPoiClickListener onPoiClickListener) {
        mOnPoiClickListener = onPoiClickListener;
    }

    @Override
    public void setOnCameraMoveStartedListener(MapContract.OnCameraMoveStartedListener onCameraMoveStartedListener) {
        mOnCameraMoveStartedListener = onCameraMoveStartedListener;
    }

    @Override
    public void enableMyLocation(boolean enable) {
        if (null != mMap) {
            //ACCESS_COARSE_LOCATION 允许 API 利用 WiFi 或移动蜂窝数据（或同时利用两者）来确定设备位置
            //ACCESS_FINE_LOCATION 允许 API 利用包括全球定位系统 (GPS) 在内的可用位置提供商以及 WiFi 和移动蜂窝数据尽可能精确地确定位置
            //if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(enable);
        }
    }

    @Override
    public Location getLastLocation() {
        //if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        return lastLocation;
    }

    /**
     * 检测是否有权限，没有权限会申请权限。
     * 申请完权限，外面的 Activity 在回调 onRequestPermissionsResult 处理打开程序最初定位功能
     * @return
     */
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(mContext, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            return false;
        }
        return true;
    }

    @Override
    public void getCurrentPlaces(final int maxSize, final MapContract.OnGetCurrentPlacesCallBack callBack) {
        if (null == callBack) {
            return;
        }

        callBack.onStart();

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callBack.onPermissionError();
        } else {
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
                    }

                    likelyPlaces.release();

                    callBack.onResult(list);
                }
            });
        }
    }

    @Override
    public List<PlaceBean> getPlacesByLatLngInBackground(double latitude, double longitude, int maxResults) {
        if (null == mGeocoder) {
            return null;
        }

        List<PlaceBean> list = new ArrayList<>();
        try {
            List<Address>  addressList =  mGeocoder.getFromLocation(latitude, longitude, maxResults);
            for (Address address : addressList) {
                //省
                String adminArea = address.getAdminArea();
                if (null == adminArea) {
                    adminArea = "";
                }
                //市
                String city = address.getLocality();
                if (null == city) {
                    city = "";
                }
                //地址
                String feature = address.getFeatureName();
                if (null == feature) {
                    feature = "";
                }

                PlaceBean bean = new PlaceBean();
                if (null != bean) {
                    bean.setSelected(false);
                    bean.setLongitude(address.getLongitude());
                    bean.setLatitude(address.getLatitude());
                    bean.setPlaceName(feature);
                    bean.setPlacePhone(address.getPhone());
                    bean.setAddress(adminArea + city + feature);
                    bean.setLocality(city);
                    bean.setFeatureName(feature);
                }
                list.add(bean);
            }

        } catch (Exception e) {
        }
        return list;
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

    @Override
    public void getPlacesByLatLng(final double latitude, final double longitude, final int maxResults, final MapContract.OnGetPlacesByLatLngCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<PlaceBean> list = getPlacesByLatLngInBackground(latitude, longitude, maxResults);
                if (null != mHandler) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != callBack) {
                                callBack.onResult(list);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void getPlaceById(String id, final int maxResult, final MapContract.OnGetPlaceByIdCallback callback) {
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, id)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        List<PlaceBean> list = null;
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            list = new ArrayList<PlaceBean>();
                            int size = places.getCount() > maxResult ? maxResult : places.getCount();
                            for (int i = 0; i < size; i++) {
                                Place place = places.get(i);

                                PlaceBean bean = getPlaceBean(place, i);
                                if (null != bean) {
                                    list.add(bean);
                                }
                            }

                        }
                        places.release();
                        if (null != callback) {
                            callback.onResult(list);
                        }
                    }
                });
    }

    @Override
    public CameraPosition getCameraPosition() {
        if (null != mMap) {
            return mMap.getCameraPosition();
        }
        return null;
    }

    @Override
    public void connect() {
        if (null != mGoogleApiClient) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void disconnect() {
        if (null != mGoogleApiClient) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void getPlacesByLatLngWeb(double latitude, double longitude, int maxResult, MapContract.OnGetPlacesByLatLngWebCallBack callback) {
        if (null == callback) {
            return;
        }
        maxResultSize = maxResult;
        mOnGetPlacesByLatLngWebCallBack = callback;
        String url = GoogleMapUrlUtil.getGoogleMapPlacesUrl(latitude, longitude);
        if (URLUtil.isNetworkUrl(url)) {
            new NearbyPlacesTask().execute(url);
        }
    }

    class NearbyPlacesTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return GoogleMapUrlUtil.returnResult(HttpUtils.doGet(params[0]));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (null != result) {
                JSONArray jsonArray = result.getJSONArray("results");
                if (null != jsonArray) {
                    List<NearbyPlaceBean> list = JSON.parseArray(jsonArray.toString(), NearbyPlaceBean.class);
                    //Log.i("lkl", "list = " + list);
                    List<PlaceBean> resultList = formatList(list, maxResultSize);

                    if (null != mOnGetPlacesByLatLngWebCallBack) {
                        mOnGetPlacesByLatLngWebCallBack.onResult(resultList);
                    }
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

    @Override
    public void getCityByLatLng(double latitude, double longitude, MapContract.OnGetCityByLatlngCallback callback) {
        if (null == callback) {
            return;
        }

        mOnGetCityByLatlngCallback = callback;

        String urlString = GoogleMapUrlUtil.getGoogleMapUrl(latitude, longitude);
        if (URLUtil.isNetworkUrl(urlString)) {
            new GeocodeCityTask().execute(urlString);
        }
    }

    /**
     * 异步任务：获取经纬度所在的城市
     */
    class GeocodeCityTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            if (null != mOnGetCityByLatlngCallback) {
                mOnGetCityByLatlngCallback.onStart();
            }
        }

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
                        if (null != mOnGetCityByLatlngCallback) {
                            mOnGetCityByLatlngCallback.onResutl(city);
                            return;
                        }
                    }
                }
            }

            if (null != mOnGetCityByLatlngCallback) {
                mOnGetCityByLatlngCallback.onError();
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

    @Override
    public void getGeocodeByLatLng(double latitude, double longitude, MapContract.OnGetGeocodeByLatLngCallback callback) {
        if (null == callback) {
            return;
        }

        mOnGetGeocodeByLatLngCallback = callback;

        String urlString = GoogleMapUrlUtil.getGoogleMapUrl(latitude, longitude);
        if (URLUtil.isNetworkUrl(urlString)) {
            new GeocodeTask().execute(urlString);
        }

    }

    /**
     * 异步任务：获取经纬度所在地理信息的对象
     */
    class GeocodeTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            if (null != mOnGetGeocodeByLatLngCallback) {
                mOnGetGeocodeByLatLngCallback.onStart();
            }
        }

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
                        if (null != mOnGetGeocodeByLatLngCallback) {
                            mOnGetGeocodeByLatLngCallback.onResutl(bean);
                            return;
                        }
                    }
                }
            }

            if (null != mOnGetGeocodeByLatLngCallback) {
                mOnGetGeocodeByLatLngCallback.onError();
            }
        }
    }
}
