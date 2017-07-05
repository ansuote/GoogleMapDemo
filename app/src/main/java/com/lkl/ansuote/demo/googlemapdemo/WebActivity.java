package com.lkl.ansuote.demo.googlemapdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lkl.ansuote.demo.googlemapdemo.base.Define;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.PlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode.AddressComponent;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode.GeocodeBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.places.NearbyPlaceBean;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util.GoogleMapUrlUtil;
import com.lkl.ansuote.demo.googlemapdemo.base.util.HttpUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by huangdongqiang on 25/06/2017.
 */
public class WebActivity extends AppCompatActivity {

    @BindView(R.id.text_content)
    TextView mContentText;
    private int maxResultSize = 10;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_get_city_by_latlng)
    void clickGetCityByLatlng() {
        //TEST 测试地点，深圳世界之窗
        getCityByLatlngWeb(Define.WINDOW_OF_WORLD_LAT, Define.WINDOW_OF_WORLD_LNG);
    }

    /**
     * 根据经纬度获取对应的城市
     * @param latitude
     * @param longitude
     */
    private void getCityByLatlngWeb(double latitude, double longitude) {
        String urlString = GoogleMapUrlUtil.getGoogleMapUrl(latitude, longitude);
        if (URLUtil.isNetworkUrl(urlString)) {
            new GeocodeTask().execute(urlString);
        }
    }

    class GeocodeTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            showContentText(getString(R.string.client_text_loading));
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
                        showContentText(getString(R.string.web_btn_get_city_by_latlng_result, city));
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

    /**
     * 获取经纬度对应的附近地点
     * @param latitude
     * @param longitude
     */
    private void getPlacesByLatLngWeb(double latitude, double longitude) {
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
                    List<PlaceBean> resultList = formatList(list, maxResultSize);
                    StringBuilder builder = new StringBuilder();
                    for (PlaceBean bean : resultList) {
                        String address = bean.getPlaceName() + "\n";
                        builder.append(address);
                    }
                    showContentText(getString(R.string.web_btn_nearby_places_result, builder.toString()));
                }
            }

        }
    }

    @OnClick(R.id.btn_nearby_places)
    void clickNearbyPlaces() {
        showContentText(getString(R.string.client_text_loading));
        //TEST 测试地点，深圳世界之窗
        getPlacesByLatLngWeb(Define.WINDOW_OF_WORLD_LAT, Define.WINDOW_OF_WORLD_LNG);
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

}
