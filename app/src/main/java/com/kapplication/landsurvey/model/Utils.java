package com.kapplication.landsurvey.model;

import com.google.android.gms.maps.model.LatLng;

public class Utils {

    private static double EARTH_RADIUS = 6378137; // 赤道半径(单位m)

    /* 角度弧度计算公式 rad:(). <br/>	 * 	 * 360度=2π π=Math.PI	 * 	 * x度 = x*π/360 弧度 */
    private static double getRadian(double degree) {
        return degree * Math.PI / 180.0;
    }

    // 两个经纬度点之前的距离,单位m 算法:
    public static double getDistance(LatLng ll1, LatLng ll2) {
        return getDistance(ll1.latitude, ll1.longitude, ll2.latitude, ll2.longitude);
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = getRadian(lat1);
        double radLat2 = getRadian(lat2);
        double a = radLat1 - radLat2;
        double b = getRadian(lng1) - getRadian(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
}
