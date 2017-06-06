package com.lkl.ansuote.demo.googlemapdemo.main;

import android.Manifest;
import android.content.Intent;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.lkl.ansuote.demo.googlemapdemo.R;
import com.lkl.ansuote.demo.googlemapdemo.base.mode.geocode.AddressComponent;
import com.lkl.ansuote.demo.googlemapdemo.base.mode.geocode.GeocodeBean;
import com.lkl.ansuote.demo.googlemapdemo.base.util.HttpUtils;
import com.lkl.ansuote.demo.googlemapdemo.base.util.LocationTransformUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.util.PermissionUtils;
import com.lkl.ansuote.demo.googlemapdemo.base.util.Util;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.lkl.ansuote.demo.googlemapdemo.R.id.map;

public class MapsActivity extends AppCompatActivity
        implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 2;
    private int PLACE_PICKER_REQUEST = 1;
    private double mLatitude;
    private double mLongitude;

    @BindView(R.id.text_tap) TextView mTapTextView;
    @BindView(R.id.text_camera) TextView mCameraTextView;
    @BindView(R.id.img_snapshot) ImageView mSnapShotImg;

    private boolean mWaitForMapLoaded;  //等待地图加载完再拍快照
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private final int mMaxEntries = 5;
    private String[] mLikelyPlaceNames = new String[mMaxEntries];
    private String[] mLikelyPlaceAddresses = new String[mMaxEntries];
    private String[] mLikelyPlaceAttributions = new String[mMaxEntries];
    private LatLng[] mLikelyPlaceLatLngs = new LatLng[mMaxEntries];
    private String mPlaceId;
    private Handler mHandler;
    private Geocoder mGeocoder;
    private int MAX_RESULTS = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        initGoogleMap();
        createGoogleApiClient();

        initGeocode();
        mHandler = new Handler();
    }

    /**
     * 用于通过经纬度查询到附近地点
     */
    private void initGeocode() {
        mGeocoder = new Geocoder(this, Locale.getDefault());
        //mGeocoder = new Geocoder(this, Locale.JAPAN);
    }

    private void initGoogleMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(mOnMapReadyCallback);
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

    private void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mOnConnectionFailedListener)
                    .addApi(LocationServices.API)
                    .enableAutoManage(this, mOnConnectionFailedListener)
                    .build();
        }
    }

    private void lauchPlacePicker() {
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            // ...
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            // ...
            e.printStackTrace();
        }
    }

    private void regEvent(boolean b) {
        mMap.setOnMyLocationButtonClickListener(b ? mOnMyLocationButtonClickListener : null);
        mMap.setOnMapClickListener(b ? mOnMapClickListener : null);
        mMap.setOnMapLongClickListener(b ? mOnMapLongClickListener : null);
        mMap.setOnCameraIdleListener(b ? mOnCameraIdleListener : null);
        //点击地图上的景点，可以返回包含经度/维度坐标、地点 ID 以及景点名称
        mMap.setOnPoiClickListener(b ? mOnPoiClickListener : null);
    }

    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.i("lkl", "onMapReady");
            mMap = googleMap;

            // Add a marker in Sydney and move the camera
            /*LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

            regEvent(true);

            enableMyLocation();
        }
    };

    GoogleMap.OnMyLocationButtonClickListener mOnMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            Toast.makeText(MapsActivity.this, "我在定位", Toast.LENGTH_SHORT).show();
            // Return false so that we don't consume the event and the default behavior still occurs
            // (the camera animates to the user's current position).
            return false;
        }
    };

    GoogleMap.OnMapClickListener mOnMapClickListener = new GoogleMap.OnMapClickListener(){

        @Override
        public void onMapClick(LatLng latLng) {
            if (null != mTapTextView) {
                mTapTextView.setText("点击位置坐标："
                        + latLng.latitude
                        + "; "
                        + latLng.longitude);
            }
        }
    };

    GoogleMap.OnMapLongClickListener mOnMapLongClickListener = new GoogleMap.OnMapLongClickListener(){
        @Override
        public void onMapLongClick(LatLng latLng) {
            //TODO

        }
    };

    GoogleMap.OnCameraIdleListener mOnCameraIdleListener = new GoogleMap.OnCameraIdleListener(){

        @Override
        public void onCameraIdle() {
            Log.i("lkl", "onCameraIdle");
            if (null != mCameraTextView) {
                mCameraTextView.setText("当前位置坐标："
                        + mMap.getCameraPosition().target.latitude
                        + "; "
                        + mMap.getCameraPosition().target.longitude);
            }
        }
    };

    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.i("lkl", "googleApiClient -- onConnected");

            getDeviceLocation();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i("lkl", "googleApiClient -- onConnectionSuspended");
        }
    };

    GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.i("lkl", "onConnectionFailed");
        }
    };


    final GoogleMap.SnapshotReadyCallback mSnapshotReadyCallback = new GoogleMap.SnapshotReadyCallback() {
        @Override
        public void onSnapshotReady(Bitmap snapshot) {
            // Callback is called from the main thread, so we can modify the ImageView safely.
            if (null != snapshot && null != mSnapShotImg) {
                mSnapShotImg.setImageBitmap(snapshot);
            }
        }
    };

    GoogleMap.OnPoiClickListener mOnPoiClickListener = new GoogleMap.OnPoiClickListener() {
        @Override
        public void onPoiClick(PointOfInterest pointOfInterest) {
            Toast.makeText(getApplicationContext(), "Clicked: " +
                            pointOfInterest.name + "\nPlace ID:" + pointOfInterest.placeId +
                            "\nLatitude:" + pointOfInterest.latLng.latitude +
                            " Longitude:" + pointOfInterest.latLng.longitude,
                    Toast.LENGTH_SHORT).show();
            mPlaceId = pointOfInterest.placeId;
        }
    };

    /**
     * clear marker, Add a marker and move the camera
     * @param latitude
     * @param longitude
     */
    private void moveToPosition(double latitude, double longitude){
        LatLng myLoaction = new LatLng(latitude, longitude);
        if (null != mMap && null != myLoaction) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(myLoaction).title("My location"));
            //会导致定位不准确
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoaction));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 500, null);

            //最好定位的时候指定缩放级别
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latitude, longitude), DEFAULT_ZOOM));

        }
    }

    private void moveToPosition(LatLng latLng) {
        if (null == latLng) {
            return;
        }

        if (null != mMap) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("My location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }

    }

    /**
     * 清除之前的标注
     */
    private void clearMarker() {
        if (null != mMap) {
            mMap.clear();
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (checkLocationPermission()) {
            // Access to the location has been granted to the app.
            if (null != mMap) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                moveToPosition(place.getLatLng());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
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
                    enableMyLocation();

                    //TODO

                } else {
                    Log.i("lkl", "permission has not been granted");
                }
                return;
            }
        }
    }

    /**
     * if permission to access the loaction is missing,
     * and then requestPermission
     *
     * @return  true: permission has been granted   false: missing
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

    @OnClick(R.id.btn_place_picker) void clickPlacePickerBtn() {
        lauchPlacePicker();
    }

    @OnClick(R.id.btn_custom_loaction) void clickCustomLoaction() {
        LatLng myLoaction = new LatLng(mLatitude, mLongitude);
        moveToPosition(LocationTransformUtil.transformFromWGSToGCJ(myLoaction));
    }

    @OnClick(R.id.btn_snapshot) void clickSnapShot(){
        takeSnapshot();
    }

    @OnClick(R.id.btn_location_search) void clickLoactionSearch() {
        openAutocompleteActivity();
    }

    @OnClick(R.id.btn_current_location) void clickCurrentLocation() {
        getDeviceLocation();
    }

    @OnClick(R.id.btn_current_places) void getCurrentPlaces(){
        if (checkLocationPermission()) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission")
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                    int i = 0;
                    mLikelyPlaceNames = new String[mMaxEntries];
                    mLikelyPlaceAddresses = new String[mMaxEntries];
                    mLikelyPlaceAttributions = new String[mMaxEntries];
                    mLikelyPlaceLatLngs = new LatLng[mMaxEntries];
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        // Build a list of likely places to show the user. Max 5.
                        mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                        mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
                        mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                .getAttributions();
                        mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                        i++;

                        mPlaceId = (String) placeLikelihood.getPlace().getId();

                        if (i > (mMaxEntries - 1)) {
                            break;
                        }
                    }
                    // Release the place likelihood buffer, to avoid memory leaks.
                    likelyPlaces.release();

                    StringBuilder builder = new StringBuilder();
                    for (String placeName : mLikelyPlaceNames) {
                        builder.append(placeName + "\n");
                    }
                    Toast.makeText(MapsActivity.this, builder.toString(), Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    @OnClick(R.id.btn_id_places) void getPlacesById(){
        if (TextUtils.isEmpty(mPlaceId)) {
            return;
        }

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, mPlaceId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            Toast.makeText(MapsActivity.this, "myPlace = " + myPlace.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MapsActivity.this,  "Place not found", Toast.LENGTH_SHORT).show();
                        }
                        places.release();
                    }
                });
    }

    @OnClick(R.id.btn_location_by_latLng) void clickLocationByLatLng() {
        //获取当前camera位置附近的地点
        getLocationByLatLng(new GetLocationByLatLngCallback() {
            @Override
            public void onResult(List<Address> addressList) {
                StringBuilder builder = new StringBuilder();
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

                    builder.append(adminArea).append("; ").append(adminArea).append("; ").append(feature).append("\n");
                }

                Toast.makeText(MapsActivity.this, builder.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @OnClick(R.id.btn_location_city) void findCity(){
        if (null == mMap) {
            return;
        }
        //当前摄像头所在坐标
        final double latitude = mMap.getCameraPosition().target.latitude;
        final double longitude = mMap.getCameraPosition().target.longitude;

        String urlString = Util.getGoogleMapUrl(latitude, longitude);
        if (URLUtil.isNetworkUrl(urlString)) {
            new GeocodeTask().execute(urlString);
        }
    }

    /**
     * 通过经纬度获取附近的地点
     * mGeocoder.getFromLocation 的方式，在国外会造成获取为null的情况，
     * 修改地区 mGeocoder = new Geocoder(this, Locale.JAPAN) 也无效。
     * 所以改为使用 HTTP 访问 Google Maps Geocoding API，之后解析 JSON 的方式
     * @param callback
     */
    private void getLocationByLatLng(final GetLocationByLatLngCallback callback) {
        if (null == callback) {
            return;
        }

        if (null == mGeocoder || !mGeocoder.isPresent()) {
            return;
        }
        if (null == mMap && null == mMap.getCameraPosition() || null == mMap.getCameraPosition().target) {
            return;
        }

        final double latitude = mMap.getCameraPosition().target.latitude;
        final double longitude = mMap.getCameraPosition().target.longitude;

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final List<Address> addressList =  mGeocoder.getFromLocation(latitude, longitude, MAX_RESULTS);
                    if (null != mHandler) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (null != callback) {
                                    callback.onResult(addressList);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    if (null != callback) {
                        callback.onError(e);
                    }
                    e.printStackTrace();
                }

            }
        }).start();
    }

    interface GetLocationByLatLngCallback{
        void onResult(List<Address> addressList);
        void onError(Exception e);
    }


    /**
     * 获取设备的位置
     */
    private void getDeviceLocation() {

        if (checkLocationPermission())
        {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);


            if (null != mLastKnownLocation) {
                /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));*/
                moveToPosition(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            } else {
                //Current location is null. Using defaults
            }
        }
    }

    /**
     * 打开搜索的 Activity
     */
    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            // MODE_FULLSCREEN 全屏方式启动一个 Activity
            // MODE_OVERLAY 启动浮在界面上的控件
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 拍照
     */
    private void takeSnapshot() {
        if (mMap == null || null == mSnapShotImg) {
            return;
        }


        if (mWaitForMapLoaded) {
           mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
               @Override
               public void onMapLoaded() {
                   if (null != mMap) {
                       mMap.snapshot(mSnapshotReadyCallback);
                   }
               }
           });
        } else {
            mMap.snapshot(mSnapshotReadyCallback);
        }
    }


    class GeocodeTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected com.alibaba.fastjson.JSONObject doInBackground(String... params) {
            return Util.returnResult(HttpUtils.doGet(params[0]));
        }

        @Override
        protected void onPostExecute(com.alibaba.fastjson.JSONObject result) {
            Log.i("lkl", "result = " + result);
            if (null != result) {
                JSONArray jsonArray = result.getJSONArray("results");
                if (null != jsonArray) {
                    Object firstObj = jsonArray.get(0);
                    if (null != firstObj) {
                        GeocodeBean bean = JSON.parseObject(firstObj.toString(), GeocodeBean.class);
                        String city = getLocality(bean);
                        Toast.makeText(MapsActivity.this, "city = " + city, Toast.LENGTH_SHORT).show();
                    }
                }
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
    protected void onDestroy() {

        super.onDestroy();
        regEvent(false);
    }
}
