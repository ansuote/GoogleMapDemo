package com.lkl.ansuote.demo.googlemapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.lkl.ansuote.demo.googlemapdemo.base.util.PermissionUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GoogleClientActivity extends AppCompatActivity {
    private GoogleApiClient mGoogleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 1500; /* 1.5 sec */
    private boolean mConnected = false;
    private int time;
    private final int MAX_TIME = 2; //最多定位次数
    private Handler mHandler;
    private Geocoder mGeocoder;
    private int MAX_RESULTS = 10;

    @BindView(R.id.text_content)
    TextView mContentText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_client);
        ButterKnife.bind(this);
        setTitle(R.string.main_btn_client);

        createGoogleApiClient();
        initGeocode();
        mHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != mGoogleApiClient) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mGoogleApiClient) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * 用于通过经纬度查询到附近地点
     */
    private void initGeocode() {
        mGeocoder = new Geocoder(this, Locale.getDefault());
        //设置区域
        //mGeocoder = new Geocoder(this, Locale.JAPAN);
    }

    @OnClick(R.id.btn_current_latlng)
    void getCurrentLatlnt() {
        if (null == mContentText) {
            return;
        }

        showContentText(getString(R.string.client_text_loading));

        if (!mConnected) {
            showContentText(getString(R.string.client_connect_failed));
            return;
        }

        if (!checkLocationPermission()) {
            showContentText(getString(R.string.cilent_permission_failed));
            return;
        }

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (null != lastLocation) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            showContentText(getString(R.string.client_current_latlng, latitude, longitude));
        } else {
            //如果获取不到位置信息，注册位置变化监听
            regLocationUpdates();
        }
    }

    private void regLocationUpdates() {
        if (!checkLocationPermission()) {
            showContentText(getString(R.string.cilent_permission_failed));
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                locationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        time++;

                        //如果获取到位置信息，则移除位置变化监听
                        if (null != location) {
                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                            showContentText(getString(R.string.client_current_latlng, location.getLatitude(), location.getLongitude()));
                            return;
                        }

                        //如果超过最大的定位次数则停止位置变化监听
                        if (time == MAX_TIME) {
                            //移除位置变化监听
                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                            showContentText(getString(R.string.client_current_latlng_failed));
                        }
                    }
                });
    }

    /**
     * 通过经纬度获取附近的地点
     * mGeocoder.getFromLocation 的方式，在国外会造成获取为null的情况，
     * 修改地区 mGeocoder = new Geocoder(this, Locale.JAPAN) 也无效。
     * 所以改为使用 HTTP 访问 Google Maps Geocoding API，之后解析 JSON 的方式
     */
    @OnClick(R.id.btn_nearby_places)
    void clickNearbyPlacesByLatlng() {
        showContentText(getString(R.string.client_text_loading));
        //TEST 测试地点，深圳世界之窗
        double latitude = 22.5350587;
        double longitude = 113.9718932;
        getNearbyPlacesByLatlng(latitude, longitude, MAX_RESULTS);
    }

    /**
     * 获取对应经纬度附近的地点列表
     * @param latitude
     * @param longitude
     */
    private void getNearbyPlacesByLatlng(final double latitude, final double longitude, final int maxResult) {

        if (null == mGeocoder || !mGeocoder.isPresent()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final List<Address> addressList =  mGeocoder.getFromLocation(latitude, longitude, maxResult);
                    if (null != mHandler) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (null != addressList && addressList.size() > 0) {
                                    StringBuffer buffer = new StringBuffer();
                                    buffer.append(getString(R.string.client_nearby_by_latlng_start, latitude, longitude))
                                            .append(getString(R.string.client_nearby_by_latlng_result_hint));
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
                                        buffer.append(adminArea).append(city).append(feature).append("\n");
                                    }
                                    showContentText(buffer.toString());
                                } else {
                                    showContentText(getString(R.string.client_nearby_by_latlng_error));
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /**
     * 初始化 google client 用于获取地点信息
     */
    private void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            //连接成功
                            mConnected = true;
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            //连接暂停
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            //连接失败
                            mConnected = false;
                        }
                    })
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            //连接失败
                            mConnected = false;
                        }
                    })
                    .build();
        }
    }

    /**
     *
     * 检查定位权限，如果未授权则请求该权限
     * @return  true：已经授权； false：未授权
     */
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            return false;
        }
        return true;
    }

    /**
     * 显示文本
     * @param text
     */
    private void showContentText(String text) {
        if (null != mContentText && null != text) {
            mContentText.setText(text);
        }
    }
}
