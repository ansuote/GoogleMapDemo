package com.lkl.ansuote.demo.googlemapdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.PermissionUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 通过启动 Acticity 的方式启动界面
 * Created by huangdongqiang on 24/06/2017.
 */
public class StartByActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_by_activity);
        ButterKnife.bind(this);
        setTitle(R.string.main_btn_start_activity);
    }

    @OnClick(R.id.btn_search_place)
    void clickSearchPlaces() {
        openAutocompleteActivity();
    }

    @OnClick(R.id.btn_navigation)
    void clickNavigation() {
        //TEST 测试地点，深圳世界之窗
        double latitude = 22.5350587;
        double longitude = 113.9718932;
        startNavigation(latitude, longitude);
    }

    @OnClick(R.id.btn_nearby_places)
    void clickNearbyPlaces() {
        if (checkLocationPermission()) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启导航
     * @param latitude
     * @param longitude
     */
    private void startNavigation(double latitude, double longitude) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude+"&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) {
            //提示未安装google map
            Toast.makeText(this, this.getString(R.string.google_map_no_installed), Toast.LENGTH_LONG).show();
            //开启google map下载界面
            showGoogleMapDownloadView();
        }
    }

    /**
     * 开启google map下载界面
     */
    private void showGoogleMapDownloadView() {
        Uri uri = Uri.parse("market://details?id=com.google.android.apps.maps");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        this.startActivity(intent);
    }


    /**
     * 打开搜索的 Activity
     */
    private void openAutocompleteActivity() {
        try {
            // MODE_FULLSCREEN 全屏方式启动一个 Activity
            // MODE_OVERLAY 启动浮在界面上的控件
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(), 0).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                String placeText = null;
                if (null != place) {
                    placeText = "place.getId() = " + place.getId()
                              + "\nplace.getName() = " + place.getName()
                              + "\nplace.getLatLng().latitude = " + place.getLatLng().latitude
                              + "\nplace.getLatLng().longitude = " + place.getLatLng().longitude
                              + "\nplace.getAddress() = " +place.getAddress()
                              + "\nplace.getPhoneNumber() = " + place.getPhoneNumber()
                              + "\nplace.getLocale() = " + place.getLocale()
                              + "\n.......";
                }

                Toast.makeText(this, getString(R.string.start_by_activity_btn_search_place_result, placeText), Toast.LENGTH_LONG).show();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
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
}
