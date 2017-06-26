package com.lkl.ansuote.demo.googlemapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.lkl.ansuote.demo.googlemapdemo.base.util.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by huangdongqiang on 24/06/2017.
 */
public class PlacesActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final int mMaxEntries = 5;
    private GoogleApiClient mGoogleApiClient;
    private boolean mConnected = false;
    @BindView(R.id.text_content)
    TextView mContentText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        ButterKnife.bind(this);
        setTitle(R.string.main_btn_places);

        createGoogleApiClient();
    }

    @Override
    protected void onStart() {
        if (null != mGoogleApiClient) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @OnClick(R.id.btn_current_places)
    void clickCurrentPlaces(){
        showContentText(getString(R.string.client_text_loading));
        getCurrentPlaces();
    }

    @OnClick(R.id.btn_get_place_by_place_id)
    void getPlaceByPlaceId() {
        showContentText(getString(R.string.client_text_loading));
        //TEST 测试PlaceId为 宝安中心地铁站
        String placeId = "ChIJdXuTpiDsAzQRHMW47SojO70";
        getPlaceById(placeId);
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
     * 获取当前位置及附近地点
     */
    private void getCurrentPlaces(){
        if (!mConnected) {
            return;
        }

        if (!checkLocationPermission()) {
            showContentText(getString(R.string.cilent_permission_failed));
        }

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                int i = 0;
                String[] likelyPlaceNames = new String[mMaxEntries];
                String[] likelyPlaceAddresses = new String[mMaxEntries];
                String[] likelyPlaceAttributions = new String[mMaxEntries];
                LatLng[] likelyPlaceLatLngs = new LatLng[mMaxEntries];
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    // Build a list of likely places to show the user. Max 5.
                    likelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                    likelyPlaceAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
                    likelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                            .getAttributions();
                    likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                    i++;

                    //String placeId = (String) placeLikelihood.getPlace().getId();

                    if (i > (mMaxEntries - 1)) {
                        break;
                    }
                }
                likelyPlaces.release();

                StringBuilder builder = new StringBuilder();
                for (String placeName : likelyPlaceNames) {
                    builder.append(placeName + "\n");
                }

                showContentText(getString(R.string.places_text_current_places, builder.toString()));
            }
        });
    }

    /**
     * 通过placeId获取对应的位置信息
     * @param placeId
     */
    private  void getPlaceById(String placeId){
        if (TextUtils.isEmpty(placeId)) {
            return;
        }

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            showContentText(getString(R.string.places_btn_get_place_by_place_id_result, String.valueOf(myPlace.getName())));
                        } else {
                            showContentText(getString(R.string.places_no_data));
                        }
                        places.release();
                    }
                });
    }

    /**checkLocationPermission
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
