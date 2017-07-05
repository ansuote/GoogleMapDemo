package com.lkl.ansuote.demo.googlemapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.PermissionUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.lkl.ansuote.demo.googlemapdemo.R.id.map;

/**
 * Map 相关设置
 * Created by huangdongqiang on 11/06/2017.
 */
public class MapActivity extends AppCompatActivity {
    private GoogleMap mMap;
    private boolean mLocationEnabled;       //使能定位开关按钮
    private boolean mZoomControlsEnabled;   //使能缩放开关按钮
    private boolean mZommGesturesEnabled;   //使能手势缩放按钮
    private boolean mScrollGesturesEnabled;//使能手势平移
    private boolean mCompassEnabled;        //使能指南针
    private boolean mRotateGesturesEnabled;//使能手势旋转
    private boolean mCameraListenerEnabled;//使能摄像头相关监听
    private boolean mClickEnabled;         //使能点击相关事件
    private boolean mTakeSnapshotEnabled;  //使能快照功能
    private boolean mWaitForMapLoaded = true;//是否等待地图完全加载

    private final float ZOOM_LEVEL = 18;    //摄像头放大级别

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        setTitle(R.string.main_btn_map);

        initGoogleMap();
    }

    private void initGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //初始化完 GoogleMap
                mMap = googleMap;
            }
        });
    }

    @OnClick(R.id.btn_switch_location)
    void clickSwitchLocation() {
        if (!isMapReady()) {
            return;
        }

        if (checkLocationPermission()) {
            if (null != mMap) {
                mLocationEnabled = !mLocationEnabled;
                mMap.setMyLocationEnabled(mLocationEnabled);
                mMap.setOnMyLocationButtonClickListener(mLocationEnabled ?new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        Toast.makeText(MapActivity.this, getString(R.string.map_switch_location_listener_tip), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }:null);
                showBtnClickTip(mLocationEnabled, getString(R.string.map_btn_switch_location));
            }
        }
    }

    /**
     * 显示按钮点击提示
     * @param locationEnabled
     * @param text
     */
    private void showBtnClickTip(boolean locationEnabled, String text) {
        String preText = getString(locationEnabled ? R.string.map_btn_open_tip : R.string.map_btn_close_tip);
        Toast.makeText(this, preText + text, Toast.LENGTH_SHORT).show();
    }

    private void showBtnClickTip(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_switch_zoom_controls)
    void clickSwitchZoomControls() {
        if (!isMapReady()) {
            return;
        }

        mZoomControlsEnabled = !mZoomControlsEnabled;
        UiSettings uiSettings = mMap.getUiSettings();
        if (null != uiSettings) {
            uiSettings.setZoomControlsEnabled(mZoomControlsEnabled);
        }
        showBtnClickTip(mZoomControlsEnabled, getString(R.string.map_btn_switch_zoom_controls));
    }

    @OnClick(R.id.btn_switch_zoom_gestures) void clickSwitchZoomGestures() {
        if (!isMapReady()) {
            return;
        }

        mZommGesturesEnabled = !mZommGesturesEnabled;
        UiSettings uiSettings = mMap.getUiSettings();
        if (null != uiSettings) {
            uiSettings.setZoomGesturesEnabled(mZommGesturesEnabled);
        }
        showBtnClickTip(mZommGesturesEnabled, getString(R.string.map_btn_switch_zoom_gestures));
    }

    @OnClick(R.id.btn_switch_scroll_gestures) void switchScrollGestures() {
        if (!isMapReady()) {
            return;
        }

        mScrollGesturesEnabled = !mScrollGesturesEnabled;
        UiSettings uiSettings = mMap.getUiSettings();
        if (null != uiSettings) {
            uiSettings.setScrollGesturesEnabled(mScrollGesturesEnabled);
        }
        showBtnClickTip(mScrollGesturesEnabled, getString(R.string.map_btn_switch_scroll_gestures));
    }

    @OnClick(R.id.btn_switch_compass)
    void clickSwitchCompass() {
        if (!isMapReady()) {
            return;
        }

        mCompassEnabled = !mCompassEnabled;
        UiSettings uiSettings = mMap.getUiSettings();
        if (null != uiSettings) {
            uiSettings.setCompassEnabled(mCompassEnabled);
        }
        showBtnClickTip(mCompassEnabled, getString(R.string.map_btn_switch_compass));

    }

    @OnClick(R.id.btn_switch_rotate)
    void clickSwitchRotate() {
        if (!isMapReady()) {
            return;
        }

        mRotateGesturesEnabled = !mRotateGesturesEnabled;
        UiSettings uiSettings = mMap.getUiSettings();
        if (null != uiSettings) {
            uiSettings.setRotateGesturesEnabled(mRotateGesturesEnabled);
        }
        showBtnClickTip(mRotateGesturesEnabled, getString(R.string.map_btn_switch_rotate));

    }

    @OnClick(R.id.btn_marker_add)
    void clickSwitchMarkerAdd() {
        if (!isMapReady()) {
            return;
        }

        //获取当前摄像头中心点的坐标
        final double latitude = mMap.getCameraPosition().target.latitude;
        final double longitude = mMap.getCameraPosition().target.longitude;

        LatLng latLng = new LatLng(latitude, longitude);


        //标记当前坐标
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_position_small))
                .title(getString(R.string.map_camera_center_location)));

        //摄像头移动到该位置
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        showBtnClickTip(getString(R.string.map_btn_marker_add));
    }

    @OnClick(R.id.btn_marker_clear)
    void clickMarkerClear() {
        if (!isMapReady()) {
            return;
        }

        mMap.clear();
        showBtnClickTip(getString(R.string.map_btn_marker_clear));
    }

    @OnClick(R.id.btn_camera_listener)
    void clickCameraListener() {
        if (!isMapReady()) {
            return;
        }

        mCameraListenerEnabled = !mCameraListenerEnabled;

        //摄像头开始滑动监听
        mMap.setOnCameraMoveStartedListener(mCameraListenerEnabled ? new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    //表示摄像头移动是为了响应用户在地图上做出的手势，如平移、倾斜、通过捏合手指进行缩放或旋转地图

                } else if (reason == GoogleMap.OnCameraMoveStartedListener
                        .REASON_API_ANIMATION) {
                    //表示 API 移动摄像头是为了响应非手势用户操作，如点按 zoom 按钮、点按 My Location 按钮或点击标记

                } else if (reason == GoogleMap.OnCameraMoveStartedListener
                        .REASON_DEVELOPER_ANIMATION) {
                    //表示您的应用已发起摄像头移动
                }
            }
        } : null);

        //摄像头移动中监听
        mMap.setOnCameraMoveListener(mCameraListenerEnabled ? new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

            }
        } : null);

        //摄像头移动停止状态的监听
        mMap.setOnCameraIdleListener(mCameraListenerEnabled ? new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //获取当前摄像头中心点的坐标
                final double latitude = mMap.getCameraPosition().target.latitude;
                final double longitude = mMap.getCameraPosition().target.longitude;
                Toast.makeText(MapActivity.this,
                        MapActivity.this.getString(R.string.map_camera_idle_tip, latitude, longitude),
                        Toast.LENGTH_SHORT).show();
            }
        } : null);

        //摄像头移动中被取消时的监听
        mMap.setOnCameraMoveCanceledListener(mCameraListenerEnabled ? new GoogleMap.OnCameraMoveCanceledListener() {
            @Override
            public void onCameraMoveCanceled() {

            }
        } : null);
    }

    @OnClick(R.id.btn_click_listener)
    void clickEventListener() {
        if (!isMapReady()) {
            return;
        }

        mClickEnabled = !mClickEnabled;
        showBtnClickTip(mClickEnabled, getString(R.string.map_btn_click_listener));

        //点击地图上某个坐标
        mMap.setOnMapClickListener(mClickEnabled ? new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MapActivity.this,
                        getString(R.string.map_click_tip, latLng.latitude, latLng.longitude),
                        Toast.LENGTH_SHORT).show();
            }
        } : null);

        //长按地图上某个坐标
        mMap.setOnMapLongClickListener(mClickEnabled ? new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(MapActivity.this,
                        getString(R.string.map_long_click_tip, latLng.latitude, latLng.longitude),
                        Toast.LENGTH_SHORT).show();
            }
        } : null);

        //点击地图上某个景点
        mMap.setOnPoiClickListener(mClickEnabled ? new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                Toast.makeText(MapActivity.this,
                        getString(R.string.map_place_click_tip, pointOfInterest.name, pointOfInterest.placeId, pointOfInterest.latLng.latitude, pointOfInterest.latLng.longitude),
                        Toast.LENGTH_SHORT).show();
            }
        } : null);
    }

    @OnClick(R.id.btn_take_snapshot)
    void clickTakeSnapshot() {
        if (!isMapReady()) {
            return;
        }

        mTakeSnapshotEnabled = !mTakeSnapshotEnabled;

        if (mWaitForMapLoaded) {
            //获取加载完的高清图片
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    if (null != mMap) {
                        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap bitmap) {
                                Toast.makeText(MapActivity.this, getString(R.string.map_take_snapshot_finish_tip), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        } else {
            //未加载完，就执行快照功能，会导致截取模糊图片
            mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    Toast.makeText(MapActivity.this, getString(R.string.map_take_snapshot_finish_tip), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 是否已经初始化谷歌地图
     * @return
     */
    private boolean isMapReady() {
        return null != mMap;
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

}
