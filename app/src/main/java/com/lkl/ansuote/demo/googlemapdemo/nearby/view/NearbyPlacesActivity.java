package com.lkl.ansuote.demo.googlemapdemo.nearby.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.lkl.ansuote.demo.googlemapdemo.R;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.HGoogleMap;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.PlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.PermissionUtils;
import com.lkl.ansuote.demo.googlemapdemo.base.mvp.BaseMVPActivity;
import com.lkl.ansuote.demo.googlemapdemo.nearby.INearbyPlacesView;
import com.lkl.ansuote.demo.googlemapdemo.nearby.NearbyPlacesPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

import static com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.HGoogleMap.LOCATION_PERMISSION_REQUEST_CODE;

/**
 * 对话界面的位置界面
 * Created by huangdongqiang on 22/05/2017.
 */
public class NearbyPlacesActivity extends BaseMVPActivity<INearbyPlacesView, NearbyPlacesPresenter>
        implements INearbyPlacesView {

    @BindView(R.id.listview)
    ListView mListView;
    private PlacesAdapter mPlacesAdapter;


    private static final int REQUEST_CODE_AUTOCOMPLETE = 2;
    private int PLACE_PICKER_REQUEST = 1;
    private AlertDialog mDialog;

    @Override
    protected NearbyPlacesPresenter createPresenter() {
        return new NearbyPlacesPresenter();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_nearby_search);
        ButterKnife.bind(this);

        mPlacesAdapter = new PlacesAdapter(this);
        if (null != mListView) {
            mListView.setAdapter(mPlacesAdapter);
        }
    }

    @Override
    public void showListView() {
        if (null != mListView) {
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideListView() {
        if (null != mListView) {
            mListView.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean checkLocationPermission() {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPresenter.onRequestPermissionsSuccessed();

                } else {
                    Toast.makeText(NearbyPlacesActivity.this, "授权失败", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void refreshListView(List<PlaceBean> list) {
        if (null != mPlacesAdapter) {
            mPlacesAdapter.setData(list);
            mPlacesAdapter.notifyDataSetChanged();
        }
    }

    @OnClick(R.id.id_search) void clickSearchBtn() {
        openAutocompleteActivity();
    }

    @OnClick(R.id.title_left_button) void clickBackBtn() {
        finish();
    }

    @OnClick(R.id.right_title_layout) void clickSendBtn() {
        mPresenter.clickSendBtn();
    }

    @OnItemClick(R.id.listview) void clickItem(int position) {
        Log.i("lkl", "clickItem");
        mPresenter.clickItem(position);
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
            startActivityForResult(intent, HGoogleMap.REQUEST_CODE_AUTOCOMPLETE);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mPresenter.clickSearchResult(place);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    @Override
    public void sendLocation(PlaceBean placeBean, Bitmap bitmap) {
        Toast.makeText(this, getString(R.string.nearby_places_send_result), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showChooseLoactionTip() {
        Toast.makeText(this, this.getString(R.string.nearby_places_choose_loaction_tip), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLoadingDialog() {
        if (null == mDialog) {
            mDialog = new AlertDialog.Builder(this).create();
        }
        if (null != mDialog) {
            mDialog.show();
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            View loading= LayoutInflater.from(this).inflate(R.layout.dialog_loading,null);
            mDialog.getWindow().setContentView(loading);
        }
    }

    @Override
    public void hideLoadingDailog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.cancel();
        }
    }

    @Override
    public void moveToFirstListItem() {
        if (null != mListView) {
            mListView.smoothScrollToPosition(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
