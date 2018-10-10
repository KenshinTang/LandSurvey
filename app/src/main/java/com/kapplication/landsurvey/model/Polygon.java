package com.kapplication.landsurvey.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;

public class Polygon {
    public LinkedList<LatLng> mPoints = new LinkedList<>();

    public Polygon() {
    }

    public void addPoint(double lat, double lng) {
        addPoint(new LatLng(lat, lng));
    }

    public void addPoint(LatLng ll) {
        mPoints.add(ll);
    }

    public void removePoint() {
        mPoints.removeLast();
    }

    public double getArea() {
        return 0d;
    }

    public double getPerimeter() {
        double perimeter = 0d;
        LatLng temp = mPoints.getFirst();
        for (LatLng p: mPoints) {
            perimeter += Utils_B.getDistance(temp, p);
            temp = p;
        }
        return perimeter;
    }
}
