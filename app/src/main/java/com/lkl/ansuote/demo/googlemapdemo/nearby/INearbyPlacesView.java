package com.lkl.ansuote.demo.googlemapdemo.nearby;
import android.graphics.Bitmap;
import com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.PlaceBean;
import java.util.List;

/**
 * Created by huangdongqiang on 22/05/2017.
 */
public interface INearbyPlacesView {

    void showLoadingDialog();

    void hideLoadingDailog();

    //void regEvent(boolean b);

    /**
     * 检查权限，如果检测权限未申请，则尝试申请权限
     * @return
     */
    boolean checkLocationPermission();

    /**
     * 显示附近地点的 ListView
     */
    void showListView();

    /**
     * 隐藏附近地点的 ListView
     */
    void hideListView();

    /**
     * 刷新列表数据
     * @param list
     */
    void refreshListView(List<PlaceBean> list);

    /**
     * 发送位置
     * @param bitmap
     */
    void sendLocation(PlaceBean placeBean, Bitmap bitmap);

    void showChooseLoactionTip();

    void moveToFirstListItem();

    /**
     * 显示GPS未开启提示
     */
    void showCheckGpsSettingsError();

    /**
     * 显示定位中提示
     */
    void showGpsDoingTip();
}
