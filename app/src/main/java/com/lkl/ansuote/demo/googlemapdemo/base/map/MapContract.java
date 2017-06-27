package com.lkl.ansuote.demo.googlemapdemo.base.map;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.PlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode.GeocodeBean;

import java.util.List;

/**
 * Created by huangdongqiang on 23/05/2017.
 */
public interface MapContract {

    interface OnMapReadyListener<T> {
        void onMapReady(T callback);
    }

    interface OnConnectionListener {

        void onConnected(@Nullable Bundle bundle);

        void onConnectionSuspended(int i);
    }

    interface OnConnectionFailedListener<T> {
        void onConnectionFailed(@NonNull T var1);
    }

    interface OnSnapshotReadyListener {
        void onSnapshotReady(Bitmap bitmap);
    }

    interface OnMyLocationButtonClickListener {
        boolean onMyLocationButtonClick();
    }

    interface OnMapClickListener {
        void onMapClick(LatLng var1);
    }

    interface OnMapLongClickListener {
        void onMapLongClick(LatLng var1);
    }

    interface OnCameraIdleListener {
        void onCameraIdle();
    }

    interface OnGetCurrentPlacesCallBack{
        void onResult(List<PlaceBean> list);
        void onPermissionError();
    }

    interface OnGetPlacesByLatLngCallBack{
        void onResult(List<PlaceBean> list);
    }

    interface OnGetPlaceByIdCallback{
        void onResult(List<PlaceBean> list);
    }

    interface OnGetPlacesByLatLngWebCallBack {
        void onResult(List<PlaceBean> list);
    }

    interface OnLocationListener<L> {
        void onLocationChanged(L location);
    }

    interface OnPoiClickListener<P>{
        void onPoiClick(P pointOfInterest);
    }

    interface OnCameraMoveStartedListener {
        int REASON_GESTURE = 1;
        int REASON_API_ANIMATION = 2;
        int REASON_DEVELOPER_ANIMATION = 3;

        void onCameraMoveStarted(int reason);
    }

    interface OnGetCityByLatlngCallback {
        void onStart();
        void onResutl(String city);
        void onError();
    }

    interface OnGetGeocodeByLatLngCallback {
        void onStart();
        void onResutl(GeocodeBean bean);
        void onError();
    }
}
