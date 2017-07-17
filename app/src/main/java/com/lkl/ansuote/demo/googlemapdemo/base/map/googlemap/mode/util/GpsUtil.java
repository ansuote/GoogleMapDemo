package com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;

import com.lkl.ansuote.demo.googlemapdemo.R;


/**
 * Created by huangdongqiang on 14/07/2017.
 */
public class GpsUtil {
    /**
     * 判断 GPS 开关【位置信息】是否开启
     * 通用方法（非Googelmap也可以用）
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //GPS卫星定位
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //WLAN或移动网络(3G/2G)
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    /**
     * 打开系统的 GPS 开关按钮
     * 通用方法（非Googelmap也可以用）
     */
    public static void showDialogGPS(final Context context) {
        if (null == context) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.dialog_gps_title));
        builder.setMessage(context.getString(R.string.dialog_gps_msg));
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(context.getString(R.string.ensure), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
