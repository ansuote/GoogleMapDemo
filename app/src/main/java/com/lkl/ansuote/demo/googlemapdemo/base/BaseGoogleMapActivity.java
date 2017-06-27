package com.lkl.ansuote.demo.googlemapdemo.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PointOfInterest;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.PermissionUtils;

import java.util.Locale;

import static com.lkl.ansuote.demo.googlemapdemo.R.id.map;

public abstract class BaseGoogleMapActivity extends AppCompatActivity {
    protected GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected Geocoder mGeocoder;

    protected static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    protected static final int REQUEST_CODE_AUTOCOMPLETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initGoogleMap();
        createGoogleApiClient();
        //initGeocode();
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

    private void initGoogleMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.i("lkl", "onMapReady");
                mMap = googleMap;
                regEvent(true);
                onMapReadyImp(googleMap);
            }
        });
    }

    public abstract void initView();
    public abstract void onMapReadyImp(GoogleMap googleMap);
    public abstract void onConnectedImp(@Nullable Bundle bundle);
    public abstract void onConnectionFailedImp(@NonNull ConnectionResult connectionResult);
    public abstract boolean onMyLocationButtonClickImp();
    public abstract void onMapClickImp(LatLng latLng);
    public abstract void onMapLongClickImp(LatLng latLng);
    public abstract void onCameraIdleImp();
    public abstract void onPoiClickImp(PointOfInterest pointOfInterest);
    public abstract void onCameraMoveStartedByGestureImp();
    public abstract void onCameraMoveStartedByApiAnimationImp();
    public abstract void onCameraMoveStartedByDeveloperAnimationImp();


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
                            onConnectedImp(bundle);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            onConnectionFailedImp(connectionResult);
                        }
                    })
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            onConnectionFailedImp(connectionResult);
                        }
                    })
                    .build();
        }
    }

    /**
     * 用于通过经纬度查询到附近地点
     */
    private void initGeocode() {
        if (null == mGeocoder) {
            mGeocoder = new Geocoder(this, Locale.getDefault());
            //mGeocoder = new Geocoder(this, Locale.JAPAN);
        }
    }


    private void regEvent(boolean b) {
        mMap.setOnMyLocationButtonClickListener(b ? new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return onMyLocationButtonClickImp();
            }
        } : null);
        mMap.setOnMapClickListener(b ? new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                onMapClickImp(latLng);
            }
        } : null);
        mMap.setOnMapLongClickListener(b ? new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                onMapLongClickImp(latLng);
            }
        } : null);
        mMap.setOnCameraIdleListener(b ? new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                onCameraIdleImp();
            }
        } : null);
        //点击地图上的景点，可以返回包含经度/维度坐标、地点 ID 以及景点名称
        mMap.setOnPoiClickListener(b ? new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                onPoiClickImp(pointOfInterest);
            }
        } : null);
        mMap.setOnCameraMoveStartedListener(b ? (new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    onCameraMoveStartedByGestureImp();
                } else if (reason == GoogleMap.OnCameraMoveStartedListener
                        .REASON_API_ANIMATION) {
                    onCameraMoveStartedByApiAnimationImp();

                } else if (reason == GoogleMap.OnCameraMoveStartedListener
                        .REASON_DEVELOPER_ANIMATION) {
                    onCameraMoveStartedByDeveloperAnimationImp();
                }
            }
        }) : null);
    }

    /**
     * 检查是否有定位权限
     * @return
     */
    protected boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        regEvent(false);
        mMap = null;
        if (null != mGoogleApiClient) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        mGeocoder = null;
    }
}
