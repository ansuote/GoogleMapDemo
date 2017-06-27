package com.lkl.ansuote.demo.googlemapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.lkl.ansuote.demo.googlemapdemo.nearby.view.NearbyPlacesActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btn_map) Button mMapBtn;
    @BindView(R.id.btn_client) Button mClientBtn;
    @BindView(R.id.btn_start_activity) Button mStratActivityBtn;
    @BindView(R.id.btn_places) Button mPlacesBtn;
    @BindView(R.id.btn_web_api) Button mWebApiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_map)
    void clickMap() {
        startActivity(new Intent(this, MapActivity.class));
    }

    @OnClick(R.id.btn_client)
    void clickClient() {
        startActivity(new Intent(this, GoogleClientActivity.class));
    }

    @OnClick(R.id.btn_start_activity)
    void clickStartActivity() {
        startActivity(new Intent(this, StartByActivity.class));
    }

    @OnClick(R.id.btn_places)
    void clickPlaces() {
        startActivity(new Intent(this, PlacesActivity.class));
    }

    @OnClick(R.id.btn_web_api)
    void clickWebApi() {
        startActivity(new Intent(this, WebActivity.class));
    }

    @OnClick(R.id.btn_nearby_demo)
    void clickDemo() {
        startActivity(new Intent(this, NearbyPlacesActivity.class));
    }

}
